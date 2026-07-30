package com.mimo.gstbilling.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.LedgerEntryEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.LedgerViewModel
import com.mimo.gstbilling.utils.ReconciliationMatch
import com.mimo.gstbilling.utils.ReconciliationSummary
import com.mimo.gstbilling.utils.SmartReconciler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReconciliationScreen(
    navController: NavController,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val ledgerData by viewModel.ledgerData.collectAsState()
    var matches by remember { mutableStateOf<List<ReconciliationMatch>>(emptyList()) }
    var summary by remember { mutableStateOf<ReconciliationSummary?>(null) }
    var selectedMatchIds by remember { mutableStateOf(setOf<Long>()) }
    var isProcessing by remember { mutableStateOf(false) }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBankStatement(navController.context, it) }
    }

    LaunchedEffect(ledgerData) {
        if (ledgerData.unreconciledApp.isNotEmpty() && ledgerData.unreconciledBank.isNotEmpty()) {
            isProcessing = true
            val result = SmartReconciler.findMatches(ledgerData.unreconciledApp, ledgerData.unreconciledBank)
            matches = result
            summary = SmartReconciler.getSummary(result)
            isProcessing = false
        } else {
            matches = emptyList()
            summary = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Reconciliation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            if (matches.isNotEmpty()) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedMatchIds = emptySet() },
                            modifier = Modifier.weight(1f),
                            shape = VyaparButtonShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text("Clear Selection", fontSize = 13.sp)
                        }
                        Button(
                            onClick = {
                                val selected = matches.filter {
                                    it.appEntry.id in selectedMatchIds
                                }
                                selected.forEach { match ->
                                    viewModel.reconcileEntries(match.appEntry.id, match.bankEntry.id)
                                }
                                selectedMatchIds = emptySet()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = selectedMatchIds.isNotEmpty(),
                            shape = VyaparButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VyaparButtonBlue,
                                disabledContainerColor = Divider
                            )
                        ) {
                            Text(
                                "Reconcile Selected (${selectedMatchIds.size})",
                                fontSize = 13.sp,
                                color = if (selectedMatchIds.isNotEmpty()) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            item {
                EntryCountHeader(
                    appCount = ledgerData.unreconciledApp.size,
                    bankCount = ledgerData.unreconciledBank.size
                )
            }

            item {
                ActionButtons(
                    onImport = {
                        csvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv"))
                    },
                    onAutoReconcile = {
                        isProcessing = true
                        val result = SmartReconciler.findMatches(ledgerData.unreconciledApp, ledgerData.unreconciledBank)
                        matches = result
                        summary = SmartReconciler.getSummary(result)
                        isProcessing = false
                    },
                    hasEntries = ledgerData.unreconciledApp.isNotEmpty() && ledgerData.unreconciledBank.isNotEmpty()
                )
            }

            if (isProcessing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VyaparButtonBlue)
                    }
                }
            }

            summary?.let { s ->
                if (s.totalMatches > 0) {
                    item {
                        SummaryBar(s)
                    }
                }
            }

            val autoMatched = matches.filter { it.score >= 0.8f }
            val needsReview = matches.filter { it.score in 0.5f..0.79f }
            val lowConfidence = matches.filter { it.score < 0.5f }

            if (autoMatched.isNotEmpty()) {
                item {
                    SectionHeader("Auto-Matched", VyaparGreen, autoMatched.size)
                }
                items(autoMatched, key = { it.appEntry.id }) { match ->
                    MatchCard(
                        match = match,
                        isSelected = match.appEntry.id in selectedMatchIds,
                        onToggleSelect = {
                            selectedMatchIds = if (match.appEntry.id in selectedMatchIds) {
                                selectedMatchIds - match.appEntry.id
                            } else {
                                selectedMatchIds + match.appEntry.id
                            }
                        },
                        onReconcile = {
                            viewModel.reconcileEntries(match.appEntry.id, match.bankEntry.id)
                        }
                    )
                }
            }

            if (needsReview.isNotEmpty()) {
                item {
                    SectionHeader("Needs Review", VyaparOrange, needsReview.size)
                }
                items(needsReview, key = { it.appEntry.id }) { match ->
                    MatchCard(
                        match = match,
                        isSelected = match.appEntry.id in selectedMatchIds,
                        onToggleSelect = {
                            selectedMatchIds = if (match.appEntry.id in selectedMatchIds) {
                                selectedMatchIds - match.appEntry.id
                            } else {
                                selectedMatchIds + match.appEntry.id
                            }
                        },
                        onReconcile = {
                            viewModel.reconcileEntries(match.appEntry.id, match.bankEntry.id)
                        }
                    )
                }
            }

            if (lowConfidence.isNotEmpty()) {
                item {
                    SectionHeader("Low Confidence", VyaparRed, lowConfidence.size)
                }
                items(lowConfidence, key = { it.appEntry.id }) { match ->
                    MatchCard(
                        match = match,
                        isSelected = match.appEntry.id in selectedMatchIds,
                        onToggleSelect = {
                            selectedMatchIds = if (match.appEntry.id in selectedMatchIds) {
                                selectedMatchIds - match.appEntry.id
                            } else {
                                selectedMatchIds + match.appEntry.id
                            }
                        },
                        onReconcile = {
                            viewModel.reconcileEntries(match.appEntry.id, match.bankEntry.id)
                        }
                    )
                }
            }

            if (!isProcessing && matches.isEmpty() && ledgerData.unreconciledApp.isNotEmpty() && ledgerData.unreconciledBank.isNotEmpty()) {
                item {
                    EmptyState("No matches found. Try importing a different bank statement.")
                }
            }

            if (ledgerData.unreconciledApp.isEmpty() || ledgerData.unreconciledBank.isEmpty()) {
                item {
                    EmptyState(
                        if (ledgerData.unreconciledBank.isEmpty())
                            "Import a bank statement to start reconciliation."
                        else
                            "No unreconciled app entries."
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun EntryCountHeader(appCount: Int, bankCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = VyaparButtonBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$appCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    "Your Entries",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = VyaparGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$bankCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    "Bank Entries",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onImport: () -> Unit,
    onAutoReconcile: () -> Unit,
    hasEntries: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onImport,
            modifier = Modifier.weight(1f),
            shape = VyaparButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparButtonBlue),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
        ) {
            Icon(
                Icons.Default.Upload,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Import CSV", fontSize = 13.sp)
        }
        Button(
            onClick = onAutoReconcile,
            modifier = Modifier.weight(1f),
            enabled = hasEntries,
            shape = VyaparButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = VyaparButtonBlue,
                disabledContainerColor = Divider
            )
        ) {
            Icon(
                Icons.Default.AutoFixHigh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (hasEntries) Color.White else TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Auto-Reconcile All",
                fontSize = 13.sp,
                color = if (hasEntries) Color.White else TextSecondary
            )
        }
    }
}

@Composable
private fun SummaryBar(summary: ReconciliationSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("${summary.autoReconciled}", "Auto-matched", VyaparGreen)
            SummaryItem("${summary.manualReview}", "Need review", VyaparOrange)
            SummaryItem("${summary.totalMatches - summary.autoReconciled - summary.manualReview}", "Unmatched", VyaparRed)
        }
    }
}

@Composable
private fun SummaryItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Text(
            label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun SectionHeader(title: String, color: Color, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "$count",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun MatchCard(
    match: ReconciliationMatch,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onReconcile: () -> Unit
) {
    val scoreColor = when {
        match.score >= 0.8f -> VyaparGreen
        match.score >= 0.5f -> VyaparOrange
        else -> VyaparRed
    }
    val scoreLabel = "${(match.score * 100).toInt()}%"
    val borderColor = if (isSelected) VyaparButtonBlue else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VyaparLightBlue else Color.White
        ),
        onClick = onToggleSelect
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = VyaparButtonBlue,
                        uncheckedColor = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        match.appEntry.partyName.ifBlank { "Your Entry" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "₹${String.format("%.2f", if (match.appEntry.debit > 0) match.appEntry.debit else match.appEntry.credit)} · ${SmartReconciler.formatTimestamp(match.appEntry.date)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scoreColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        scoreLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        match.bankEntry.description.take(20).ifBlank { "Bank Entry" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "₹${String.format("%.2f", if (match.bankEntry.debit > 0) match.bankEntry.debit else match.bankEntry.credit)} · ${SmartReconciler.formatTimestamp(match.bankEntry.date)}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (match.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    match.reasons.take(3).forEach { reason ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VyaparLightBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                reason,
                                fontSize = 10.sp,
                                color = VyaparButtonBlue
                            )
                        }
                    }
                }
            }

            if (match.amountDiff > 0.01) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Amount diff: ₹${String.format("%.2f", match.amountDiff)}",
                    fontSize = 10.sp,
                    color = VyaparOrange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onReconcile,
                    colors = ButtonDefaults.textButtonColors(contentColor = VyaparButtonBlue)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reconcile", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = VyaparEmptyStateIcon,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                message,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

package com.mimo.gstbilling.ui.screens

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.LedgerEntryEntity
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.LedgerViewModel
import com.mimo.gstbilling.ui.viewmodel.MatchResult
import java.text.SimpleDateFormat
import java.util.*

private val months = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return sdf.format(Date(timestamp))
}

private fun formatCurrency(value: Double): String {
    return "\u20B9${String.format("%,.2f", value)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    navController: NavController,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Ledger", "Bank Reconciliation", "GST Reports")
    val ledgerData by viewModel.ledgerData.collectAsState()
    val matchResults by viewModel.matchResults.collectAsState()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importBankStatement(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ledger", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VyaparTopBarBackground,
                    titleContentColor = VyaparTopBarText,
                    navigationIconContentColor = VyaparTopBarIcon
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = VyaparWhite,
                contentColor = VyaparTextPrimary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = VyaparTabIndicator
                        )
                    }
                },
                divider = { HorizontalDivider(color = VyaparDivider) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) VyaparTabSelectedText else VyaparTabText
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> LedgerTab(
                    ledgerData = ledgerData,
                    viewModel = viewModel
                )
                1 -> BankReconciliationTab(
                    ledgerData = ledgerData,
                    matchResults = matchResults,
                    onImportClick = { filePicker.launch(arrayOf("text/csv", "application/pdf")) },
                    onReconcile = { appEntryId, bankEntryId -> viewModel.reconcileEntries(appEntryId, bankEntryId) },
                    onReconcileAll = { viewModel.reconcileAllMatched() }
                )
                2 -> GstrReportsTab(
                    onNavigateToEWayBill = { navController.navigate(Screen.EWayBill.route) }
                )
            }
        }
    }
}

@Composable
private fun LedgerTab(
    ledgerData: com.mimo.gstbilling.ui.viewmodel.LedgerData,
    viewModel: LedgerViewModel
) {
    var selectedPartyFilter by remember { mutableLongStateOf(0L) }
    var showPartyDropdown by remember { mutableStateOf(false) }

    val partyNames = remember(ledgerData.entries) {
        ledgerData.entries.map { it.partyName }.distinct().filter { it.isNotBlank() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VyaparWhite)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Party Filter",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VyaparTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = if (selectedPartyFilter == 0L) "All Parties" else partyNames.getOrElse(selectedPartyFilter.toInt() - 1) { "All Parties" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPartyDropdown = true },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = VyaparIconDefault
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyaparInputFocused,
                            unfocusedBorderColor = VyaparInputBorder,
                            focusedContainerColor = VyaparInputBackground,
                            unfocusedContainerColor = VyaparInputBackground
                        )
                    )
                    DropdownMenu(
                        expanded = showPartyDropdown,
                        onDismissRequest = { showPartyDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Parties", fontSize = 14.sp) },
                            onClick = {
                                selectedPartyFilter = 0L
                                viewModel.selectParty(0L)
                                showPartyDropdown = false
                            }
                        )
                        partyNames.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(name, fontSize = 14.sp) },
                                onClick = {
                                    selectedPartyFilter = (index + 1).toLong()
                                    viewModel.selectParty((index + 1).toLong())
                                    showPartyDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VyaparWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Debit", fontSize = 11.sp, color = VyaparTextSecondary)
                    Text(
                        formatCurrency(ledgerData.totalDebit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VyaparStatusGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Credit", fontSize = 11.sp, color = VyaparTextSecondary)
                    Text(
                        formatCurrency(ledgerData.totalCredit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VyaparStatusRed
                    )
                }
            }
            HorizontalDivider(color = VyaparDivider, modifier = Modifier.padding(horizontal = 12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Balance", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VyaparTextPrimary)
                Text(
                    formatCurrency(ledgerData.balance),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ledgerData.balance >= 0) VyaparStatusGreen else VyaparStatusRed
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (ledgerData.entries.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ledger entries yet",
                        fontSize = 14.sp,
                        color = VyaparTextSecondary
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyaparTextSecondary, modifier = Modifier.weight(1.2f))
                        Text("Description", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyaparTextSecondary, modifier = Modifier.weight(1.5f))
                        Text("Debit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyaparTextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("Credit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyaparTextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("Balance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyaparTextSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = VyaparDivider, modifier = Modifier.padding(vertical = 6.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 500.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var runningBalance = 0.0
                        items(ledgerData.entries) { entry ->
                            runningBalance += entry.debit - entry.credit
                            LedgerRow(entry = entry, runningBalance = runningBalance)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerRow(
    entry: LedgerEntryEntity,
    runningBalance: Double
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatDate(entry.date),
                fontSize = 12.sp,
                color = VyaparTextSecondary,
                modifier = Modifier.weight(1.2f)
            )
            Text(
                entry.description,
                fontSize = 12.sp,
                color = VyaparTextPrimary,
                modifier = Modifier.weight(1.5f),
                maxLines = 1
            )
            Text(
                if (entry.debit > 0) formatCurrency(entry.debit) else "-",
                fontSize = 12.sp,
                color = VyaparStatusGreen,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                if (entry.credit > 0) formatCurrency(entry.credit) else "-",
                fontSize = 12.sp,
                color = VyaparStatusRed,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            Text(
                formatCurrency(kotlin.math.abs(runningBalance)),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (runningBalance >= 0) VyaparStatusGreen else VyaparStatusRed,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        HorizontalDivider(color = VyaparDivider, thickness = 0.5.dp)
    }
}

@Composable
private fun BankReconciliationTab(
    ledgerData: com.mimo.gstbilling.ui.viewmodel.LedgerData,
    matchResults: List<MatchResult>,
    onImportClick: () -> Unit,
    onReconcile: (Long, Long) -> Unit,
    onReconcileAll: () -> Unit
) {
    val matchedCount = remember(matchResults) {
        matchResults.count { it.bankEntry != null && it.score >= 60 }
    }
    val unmatchedCount = remember(matchResults) {
        matchResults.count { it.bankEntry == null || it.score < 60 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparButtonBlue)
            ) {
                Icon(
                    Icons.Filled.Upload,
                    contentDescription = "Import",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Statement", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (ledgerData.unreconciledApp.isNotEmpty() || ledgerData.unreconciledBank.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${ledgerData.unreconciledApp.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyaparButtonBlue
                            )
                            Text("Your Entries", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${ledgerData.unreconciledBank.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyaparAccentOrange
                            )
                            Text("Bank Entries", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$matchedCount",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyaparStatusGreen
                            )
                            Text("Matched", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$unmatchedCount",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyaparStatusRed
                            )
                            Text("Unmatched", fontSize = 11.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            if (matchedCount > 0) {
                item {
                    Button(
                        onClick = onReconcileAll,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparButtonGreen)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reconcile All Matched ($matchedCount)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Match Results",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyaparTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (matchResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No entries to match",
                                    fontSize = 13.sp,
                                    color = VyaparTextSecondary
                                )
                            }
                        } else {
                            matchResults.forEach { result ->
                                MatchResultRow(
                                    result = result,
                                    onReconcile = onReconcile
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Import a bank statement to start reconciliation",
                            fontSize = 14.sp,
                            color = VyaparTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun MatchResultRow(
    result: MatchResult,
    onReconcile: (Long, Long) -> Unit
) {
    val isMatched = result.bankEntry != null && result.score >= 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMatched) VyaparSuccessBackground.copy(alpha = 0.3f) else VyaparWhite
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        result.appEntry.description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VyaparTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        formatDate(result.appEntry.date),
                        fontSize = 11.sp,
                        color = VyaparTextSecondary
                    )
                    val amount = if (result.appEntry.debit > 0) result.appEntry.debit else result.appEntry.credit
                    Text(
                        formatCurrency(amount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.appEntry.debit > 0) VyaparStatusGreen else VyaparStatusRed
                    )
                }

                if (result.bankEntry != null) {
                    Icon(
                        Icons.Filled.Link,
                        contentDescription = "Linked",
                        tint = if (isMatched) VyaparStatusGreen else VyaparStatusOrange,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(20.dp)
                    )

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            result.bankEntry.description,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VyaparTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            formatDate(result.bankEntry.date),
                            fontSize = 11.sp,
                            color = VyaparTextSecondary
                        )
                        val bankAmount = if (result.bankEntry.debit > 0) result.bankEntry.debit else result.bankEntry.credit
                        Text(
                            formatCurrency(bankAmount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (result.bankEntry.debit > 0) VyaparStatusGreen else VyaparStatusRed
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "No match found",
                        fontSize = 12.sp,
                        color = VyaparStatusRed,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scorePercent = (result.score).toInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Match Score: ",
                        fontSize = 11.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        "$scorePercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            scorePercent >= 80 -> VyaparStatusGreen
                            scorePercent >= 60 -> VyaparStatusOrange
                            else -> VyaparStatusRed
                        }
                    )
                }

                if (isMatched && result.bankEntry != null) {
                    Button(
                        onClick = { onReconcile(result.appEntry.id, result.bankEntry.id) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparButtonGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Reconcile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GstrReportsTab(
    onNavigateToEWayBill: () -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var generatedJson by remember { mutableStateOf("") }
    var showSummary by remember { mutableStateOf(false) }

    val dummySummary = remember(showSummary) {
        if (showSummary) {
            Triple(12, 1850000.0, 333000.0)
        } else null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Report Period",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VyaparTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = "${months[selectedMonth]} $selectedYear",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMonthPicker = true },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = "Pick Month",
                                tint = VyaparIconDefault,
                                modifier = Modifier.clickable { showMonthPicker = true }
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyaparInputFocused,
                            unfocusedBorderColor = VyaparInputBorder,
                            focusedContainerColor = VyaparInputBackground,
                            unfocusedContainerColor = VyaparInputBackground
                        )
                    )
                }
            }

            DropdownMenu(
                expanded = showMonthPicker,
                onDismissRequest = { showMonthPicker = false }
            ) {
                months.forEachIndexed { index, month ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "$month $selectedYear",
                                fontSize = 14.sp,
                                fontWeight = if (index == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == selectedMonth) VyaparButtonBlue else VyaparTextPrimary
                            )
                        },
                        onClick = {
                            selectedMonth = index
                            showMonthPicker = false
                        }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    generatedJson = "{\"gstin\":\"27AALCM6709R1Z5\",\"period\":\"${months[selectedMonth]} $selectedYear\",\"b2b\":[{\"ctin\":\"29AACCT5275H1ZV\",\"inv\":[{\"inum\":\"INV-001\",\"idt\":\"2026-07-05\",\"val\":59000.0,\"pos\":\"27\",\"rev\":\"N\",\"itms\":[{\"rt\":18.0,\"txval\":50000.0,\"camt\":4500.0,\"samt\":4500.0,\"iamt\":0.0}]}]}]}"
                    showSummary = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparButtonBlue)
            ) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate GSTR-1", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (showSummary && dummySummary != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "GSTR-1 Summary",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyaparTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("B2B Invoices", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text(
                                    "${dummySummary.first}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VyaparButtonBlue
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Taxable", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text(
                                    formatCurrency(dummySummary.second),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VyaparTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Tax (CGST+SGST+IGST)", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text(
                                    formatCurrency(dummySummary.third),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VyaparStatusGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Invoice Value", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text(
                                    formatCurrency(dummySummary.second + dummySummary.third),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VyaparTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val json = generatedJson.toByteArray()
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val file = java.io.File(dir, "GSTR1_${months[selectedMonth]}_$selectedYear.json")
                        file.writeBytes(json)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparButtonGreen)
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download GSTR-1", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            HorizontalDivider(color = VyaparDivider)
        }

        item {
            Button(
                onClick = onNavigateToEWayBill,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparAccentOrange)
            ) {
                Icon(
                    Icons.Filled.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate E-Way Bill", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

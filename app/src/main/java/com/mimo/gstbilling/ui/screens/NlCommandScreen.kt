package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.utils.CommandType
import com.mimo.gstbilling.utils.NlCommandParser
import com.mimo.gstbilling.utils.NlCommandResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NlCommandScreen(
    navController: NavController,
    viewModel: com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel = hiltViewModel()
) {
    var searchText by remember { mutableStateOf("") }
    var commandHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    val parsedResult = remember(searchText) {
        if (searchText.isNotBlank()) NlCommandParser.parseCommand(searchText) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quick Commands",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Type a command...",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotBlank()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Divider,
                        focusedBorderColor = Primary,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Parsed result card
            if (parsedResult != null) {
                item {
                    CommandResultCard(
                        result = parsedResult,
                        onExecute = {
                            commandHistory = listOf(searchText) + commandHistory.take(9)
                            when (parsedResult.command) {
                                CommandType.CREATE_INVOICE -> {
                                    val party = parsedResult.params["party"] ?: ""
                                    navController.navigate(Screen.CreateInvoice.createRoute("sales"))
                                }
                                CommandType.CREATE_EXPENSE -> {
                                    navController.navigate(Screen.Expenses.route)
                                }
                                CommandType.VIEW_REPORT -> {
                                    navController.navigate(Screen.DayBookReport.route)
                                }
                                CommandType.VIEW_ITEMS -> {
                                    navController.navigate(Screen.Items.route)
                                }
                                CommandType.VIEW_PARTY -> {
                                    navController.navigate(Screen.Parties.route)
                                }
                                CommandType.HELP -> { }
                                CommandType.SEARCH -> { }
                                CommandType.UNKNOWN -> { }
                            }
                        }
                    )
                }
            }

            // Quick suggestion chips
            item {
                Text(
                    "Quick Actions",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val suggestions = listOf(
                        "Create Invoice" to CommandType.CREATE_INVOICE,
                        "Add Expense" to CommandType.CREATE_EXPENSE,
                        "View Sales" to CommandType.VIEW_REPORT,
                        "Pending Payments" to CommandType.VIEW_REPORT,
                        "Help" to CommandType.HELP
                    )
                    items(suggestions) { (label, _) ->
                        SuggestionChip(
                            label = label,
                            onClick = {
                                searchText = when (label) {
                                    "Create Invoice" -> "Create invoice"
                                    "Add Expense" -> "Add expense"
                                    "View Sales" -> "Show sales"
                                    "Pending Payments" -> "View pending payments"
                                    "Help" -> "Help"
                                    else -> label
                                }
                            }
                        )
                    }
                }
            }

            // Command history
            if (commandHistory.isNotEmpty()) {
                item {
                    Text(
                        "Recent Commands",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(commandHistory) { cmd ->
                    CommandHistoryItem(
                        command = cmd,
                        onClick = { searchText = cmd }
                    )
                }
            }

            // Help section when help command is triggered
            if (parsedResult?.command == CommandType.HELP) {
                item {
                    HelpSection()
                }
            }
        }
    }
}

@Composable
private fun CommandResultCard(
    result: NlCommandResult,
    onExecute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.command.name.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.displayText,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                }

                ConfidenceBadge(confidence = result.confidence)
            }

            if (result.params.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.params.entries.joinToString(" | ") { "${it.key}: ${it.value}" },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onExecute,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Execute", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val color = when {
        confidence >= 0.8f -> GreenBalance
        confidence >= 0.5f -> VyaparOrange
        else -> RedAccent
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = LightBlueBg
    ) {
        Text(
            text = label,
            color = Primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CommandHistoryItem(
    command: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = command,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HelpSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlueBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Available Commands",
                fontWeight = FontWeight.Bold,
                color = Primary,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val commands = listOf(
                "Create invoice for [party name]" to "Creates a new sales invoice",
                "Add expense ₹[amount] for [description]" to "Records a new expense",
                "Show sales / purchases / expenses" to "Opens respective report",
                "View pending payments" to "Shows outstanding dues",
                "Show stock / items" to "Opens inventory view",
                "View party [name]" to "Opens party details"
            )

            commands.forEach { (cmd, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column {
                        Text(
                            text = cmd,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

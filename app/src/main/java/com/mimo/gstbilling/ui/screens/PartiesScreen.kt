package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import com.mimo.gstbilling.ui.navigation.VyaparBottomBar
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val parties = uiState.allParties
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    val totalReceivable = parties.filter { it.balance > 0 }.sumOf { it.balance }
    val filteredParties = parties.filter { it.name.contains(searchText, ignoreCase = true) || (it.phone ?: "").contains(searchText) }
    var showTransactionSheet by remember { mutableStateOf(false) }

    if (showTransactionSheet) {
        TransactionTypeSheet(
            onDismiss = { showTransactionSheet = false },
            onSelect = { type ->
                showTransactionSheet = false
                when (type) {
                    "sale" -> navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "sales"))
                    "purchase" -> navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "purchase"))
                    "expense" -> navController.navigate(Screen.Expenses.route)
                    "credit_note" -> navController.navigate(Screen.CreditNote.route)
                    "debit_note" -> navController.navigate(Screen.DebitNote.route)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Parties", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = VyaparTextPrimary)
            )
        },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val selectedTab = when (currentRoute) {
                Screen.Dashboard.route -> 0
                Screen.Parties.route -> 1
                Screen.Items.route -> 3
                Screen.Settings.route -> 4
                else -> 1
            }
            VyaparBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    val targetRoute = when (tab) {
                        0 -> Screen.Dashboard.route
                        1 -> Screen.Parties.route
                        3 -> Screen.Items.route
                        4 -> Screen.Settings.route
                        else -> Screen.Dashboard.route
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddClick = { showTransactionSheet = true }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(VyaparBackground)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("\u2193", fontSize = 18.sp, color = VyaparGreen, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("You'll Get", fontSize = 14.sp, color = VyaparGreen, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\u20B9${String.format(Locale.US, "%,.2f", totalReceivable)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyaparTextPrimary
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val tabs = listOf("Parties", "Transactions", "Items")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index }
                                .background(
                                    if (isSelected) VyaparRed.copy(alpha = 0.1f) else Color.Transparent,
                                    RoundedCornerShape(50)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) VyaparRed else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) VyaparRed else VyaparTextSecondary)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = VyaparTextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("SEARCH PARTY", fontSize = 14.sp, color = VyaparTextSecondary) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { navController.navigate(Screen.AddParty.route) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparWhite),
                        border = BorderStroke(1.dp, VyaparBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("+ New Party", color = VyaparBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { navController.navigate(Screen.AddParty.route) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = VyaparTextSecondary)
                    }
                }
            }

            items(filteredParties) { party ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.PartyDetail.createRoute(party.id)) }
                        .background(VyaparWhite)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(party.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val partyInvoices = remember { invoices.filter { it.partyId == party.id } }
                            val avgQuarterly = remember(partyInvoices) {
                                if (partyInvoices.isEmpty()) 0.0
                                else {
                                    val cal = java.util.Calendar.getInstance()
                                    val now = System.currentTimeMillis()
                                    cal.timeInMillis = now
                                    val currentYear = cal.get(java.util.Calendar.YEAR)
                                    val currentQuarter = cal.get(java.util.Calendar.MONTH) / 3
                                    val quarterStart = java.util.Calendar.getInstance().apply {
                                        set(currentYear, currentQuarter * 3, 1, 0, 0, 0)
                                        set(java.util.Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    val quarterInvoices = partyInvoices.filter { it.invoiceDate >= quarterStart }
                                    val totalInQuarter = quarterInvoices.sumOf { it.totalAmount }
                                    val monthsElapsed = (currentQuarter * 3) + cal.get(java.util.Calendar.DAY_OF_MONTH) / 30.0 + 1
                                    if (monthsElapsed > 0) totalInQuarter / (monthsElapsed / 3.0) else totalInQuarter
                                }
                            }
                            if (avgQuarterly > 0) {
                                Text("Avg/Quarter: ${String.format(Locale.US, "\u20B9%,.0f", avgQuarterly)}", fontSize = 12.sp, color = VyaparBlue, fontWeight = FontWeight.Medium)
                            } else {
                                Text(dateFormat.format(Date(party.createdAt)), fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                String.format(Locale.US, "\u20B9%,.2f", party.balance),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyaparGreen
                            )
                            Text("You'll Get", fontSize = 12.sp, color = VyaparGreen)
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = VyaparDivider, thickness = 0.5.dp)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

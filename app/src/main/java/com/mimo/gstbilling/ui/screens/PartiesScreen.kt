package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val invoices by viewModel.getInvoices("sales").collectAsState(initial = emptyList())
    val parties = remember { mutableStateListOf<com.mimo.gstbilling.data.local.entity.PartyEntity>() }
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    LaunchedEffect(Unit) {
        parties.clear()
        parties.addAll(viewModel.getAllParties())
    }

    val totalReceivable = parties.filter { it.balance > 0 }.sumOf { it.balance }
    val filteredParties = parties.filter { it.name.contains(searchText, ignoreCase = true) || (it.phone ?: "").contains(searchText) }
    var showTransactionSheet by remember { mutableStateOf(false) }

    if (showTransactionSheet) {
        TransactionTypeSheet(
            onDismiss = { showTransactionSheet = false },
            onSelect = { type ->
                showTransactionSheet = false
                when (type) {
                    "sale" -> navController.navigate(Screen.CreateInvoice.route)
                    "purchase" -> navController.navigate(Screen.Purchases.route)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A))
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
                .background(LightBlueBg)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("You'll Get", fontSize = 14.sp, color = GreenBalance, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\u20B9${String.format(Locale.US, "%,.2f", totalReceivable)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val tabs = listOf("Parties", "Transactions", "Items")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(modifier = Modifier.weight(1f).clickable { selectedTab = index }.background(if (isSelected) Color(0xFFFFEBEE) else Color.Transparent).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) RedAccent else TextSecondary)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("SEARCH PARTY", fontSize = 14.sp, color = TextSecondary) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { navController.navigate(Screen.AddParty.route) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("+ New Party", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { navController.navigate(Screen.AddParty.route) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextSecondary)
                    }
                }
            }

            items(filteredParties) { party ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { navController.navigate(Screen.PartyDetail.createRoute(party.id)) },
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(party.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(dateFormat.format(Date(party.createdAt)), fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                String.format(Locale.US, "\u20B9%,.2f", party.balance),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenBalance
                            )
                            Text("You'll Get", fontSize = 12.sp, color = GreenBalance)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

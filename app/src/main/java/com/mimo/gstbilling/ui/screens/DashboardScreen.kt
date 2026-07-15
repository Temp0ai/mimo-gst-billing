package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.util.Locale

data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val hasExpand: Boolean = false,
    val subItems: List<String> = emptyList(),
    val hasNewBadge: Boolean = false,
    val subtitle: String? = null,
    val hasAddIcon: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val data by viewModel.data.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var expandedSection by remember { mutableStateOf("") }
    var showTransactionSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var partySearchQuery by remember { mutableStateOf("") }
    var transactionSearchQuery by remember { mutableStateOf("") }
    var itemSearchQuery by remember { mutableStateOf("") }

    val menuItems = listOf(
        DrawerMenuItem("Parties", Icons.Filled.Group, hasExpand = true, subItems = listOf("All Parties", "Party Groups", "Party Statement")),
        DrawerMenuItem("Items", Icons.Filled.FormatListBulleted),
        DrawerMenuItem("Business Dashboard", Icons.Filled.Category),
        DrawerMenuItem("Reports", Icons.Filled.PieChart),
        DrawerMenuItem("Sale", Icons.Filled.FormatListBulleted, hasExpand = true, subItems = listOf("All Sales", "Create Sale", "Invoice Templates", "Credit Notes")),
        DrawerMenuItem("Purchase", Icons.Filled.ShoppingCart, hasExpand = true, subItems = listOf("All Purchases", "Create Purchase", "Debit Notes")),
        DrawerMenuItem("Delivery Challans", Icons.Filled.LocalShipping),
        DrawerMenuItem("Expense", Icons.Filled.Note, hasAddIcon = true),
        DrawerMenuItem("Cash & Bank", Icons.Filled.AccountBalance, hasExpand = true, subItems = listOf("Cash Book", "Bank Accounts")),
        DrawerMenuItem("My Online Store", Icons.Filled.Store, hasExpand = true, subItems = listOf("Store Settings", "Products")),
        DrawerMenuItem("Other Products", Icons.Filled.LocalOffer),
        DrawerMenuItem("Sync & Share", Icons.Filled.CloudSync, subtitle = "Tap to sync data"),
        DrawerMenuItem("Settings", Icons.Filled.Settings, hasNewBadge = true),
        DrawerMenuItem("Backup/Restore", Icons.Filled.Warning, hasExpand = true, subtitle = "Auto backup not enabled.", subItems = listOf("Auto Backup", "Backup to phone", "Backup to e-mail", "Restore backup")),
        DrawerMenuItem("Plans & Pricing", Icons.Filled.LocalOffer, subtitle = "Free Plan"),
        DrawerMenuItem("Grow Your Business", Icons.Filled.TrendingUp, hasExpand = true, subItems = listOf("Business Dashboard", "Analytics")),
        DrawerMenuItem("Utilities", Icons.Filled.Build, hasExpand = true, subItems = listOf("Barcode Scanner", "Thermal Printer", "Import Data", "Vyapar Import")),
        DrawerMenuItem("Help & Support", Icons.Filled.Notifications, hasExpand = true, subItems = listOf("FAQs", "Contact Support"))
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Primary)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = data.companyName.take(1).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = data.companyName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "GST Registered Business",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                                scope.launch { drawerState.close() }
                                navController.navigate(Screen.CompanySwitch.route)
                            }) {
                                Text("Change Company", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                menuItems.forEach { item ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (item.hasExpand) {
                                        expandedSection = if (expandedSection == item.title) "" else item.title
                                    } else {
                                        scope.launch { drawerState.close() }
                                        when (item.title) {
                                            "Parties" -> navController.navigate(Screen.Parties.route)
                                            "Items" -> navController.navigate(Screen.Items.route)
                                            "Business Dashboard" -> navController.navigate(Screen.Dashboard.route)
                                            "Reports" -> navController.navigate(Screen.Reports.route)
                                            "Sale" -> navController.navigate(Screen.Sales.route)
                                            "Purchase" -> navController.navigate(Screen.Purchases.route)
                                            "Expense" -> navController.navigate(Screen.Expenses.route)
                                            "Cash & Bank" -> navController.navigate(Screen.CashBank.route)
                                            "My Online Store" -> navController.navigate(Screen.StoreManagement.route)
                                            "Other Products" -> navController.navigate(Screen.Items.route)
                                            "Sync & Share" -> navController.navigate(Screen.ImportData.route)
                                            "Settings" -> navController.navigate(Screen.Settings.route)
                                            "Backup/Restore" -> navController.navigate(Screen.BackupRestore.route)
                                            "Plans & Pricing" -> navController.navigate(Screen.Settings.route)
                                            "Grow Your Business" -> navController.navigate(Screen.Reports.route)
                                            "Utilities" -> navController.navigate(Screen.BarcodeScanner.route)
                                            "Help & Support" -> navController.navigate(Screen.Settings.route)
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                item.subtitle?.let {
                                    Text(it, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            if (item.hasNewBadge) {
                                Box(
                                    modifier = Modifier
                                        .background(RedAccent, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("NEW", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (item.hasAddIcon) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            } else if (item.hasExpand) {
                                Icon(
                                    if (expandedSection == item.title) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (item.hasExpand && expandedSection == item.title) {
                            item.subItems.forEach { subItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch { drawerState.close() }
                                            when (subItem) {
                                                "All Parties" -> navController.navigate(Screen.Parties.route)
                                                "Party Groups" -> navController.navigate(Screen.PartyGroups.route)
                                                "Party Statement" -> navController.navigate(Screen.PartyStatement.createRoute(1L))
                                                "All Sales" -> navController.navigate(Screen.Sales.route)
                                                "Create Sale" -> navController.navigate(Screen.CreateInvoice.route)
                                            "Invoice Templates" -> navController.navigate(Screen.InvoiceTemplates.route)
                                            "Credit Notes" -> navController.navigate(Screen.CreditNote.route)
                                            "All Purchases" -> navController.navigate(Screen.Purchases.route)
                                            "Create Purchase" -> navController.navigate(Screen.CreateInvoice.route)
                                            "Debit Notes" -> navController.navigate(Screen.DebitNote.route)
                                                "Cash Book" -> navController.navigate(Screen.CashBank.route)
                                                "Bank Accounts" -> navController.navigate(Screen.BankAccounts.route)
                                                "Store Settings" -> navController.navigate(Screen.StoreManagement.route)
                                                "Products" -> navController.navigate(Screen.Items.route)
                                                "Auto Backup" -> navController.navigate(Screen.BackupRestore.route)
                                                "Backup to phone" -> navController.navigate(Screen.BackupRestore.route)
                                                "Backup to e-mail" -> navController.navigate(Screen.BackupRestore.route)
                                                "Restore backup" -> navController.navigate(Screen.BackupRestore.route)
                                                "Business Dashboard" -> navController.navigate(Screen.Dashboard.route)
                                                "Analytics" -> navController.navigate(Screen.Reports.route)
                                                "Barcode Scanner" -> navController.navigate(Screen.BarcodeScanner.route)
                                                "Thermal Printer" -> navController.navigate(Screen.ThermalPrinter.route)
                                                "Import Data" -> navController.navigate(Screen.ImportData.route)
                                                "Vyapar Import" -> navController.navigate(Screen.VyaparDataImport.route)
                                                "FAQs" -> navController.navigate(Screen.Settings.route)
                                                "Contact Support" -> navController.navigate(Screen.Settings.route)
                                            }
                                        }
                                        .padding(start = 52.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Circle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(5.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(subItem, fontSize = 14.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(data.companyName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = Color.White)
                        }
                        IconButton(onClick = { navController.navigate(Screen.PaymentReminders.route) }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary, actionIconContentColor = TextSecondary)
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.CashBank.route) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(25.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Take Payment", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { showTransactionSheet = true },
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        Button(
                            onClick = { showTransactionSheet = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Add Sale", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F6F6)),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // You'll Get Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = GreenBalance,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("You'll Get", fontSize = 14.sp, color = GreenBalance, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "\u20B9${String.format(Locale.US, "%,d", data.pendingReceivables.toLong())}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = ".${String.format(Locale.US, "%02d", ((data.pendingReceivables % 1) * 100).toInt())}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Tab Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Parties", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(25.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEBEE),
                                selectedLabelColor = RedAccent,
                                containerColor = Color.White,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedTab == 0) RedAccent else Color(0xFFE0E0E0),
                                selectedBorderColor = RedAccent,
                                enabled = true,
                                selected = selectedTab == 0
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Transactions", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(25.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEBEE),
                                selectedLabelColor = RedAccent,
                                containerColor = Color.White,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedTab == 1) RedAccent else Color(0xFFE0E0E0),
                                selectedBorderColor = RedAccent,
                                enabled = true,
                                selected = selectedTab == 1
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Items", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(25.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEBEE),
                                selectedLabelColor = RedAccent,
                                containerColor = Color.White,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedTab == 2) RedAccent else Color(0xFFE0E0E0),
                                selectedBorderColor = RedAccent,
                                enabled = true,
                                selected = selectedTab == 2
                            )
                        )
                    }
                }

                // Parties Tab
                if (selectedTab == 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = partySearchQuery,
                                onValueChange = { partySearchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("SEARCH PARTY", fontSize = 13.sp, color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            TextButton(onClick = { navController.navigate(Screen.AddParty.route) }) {
                                Text("+ New Party", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextSecondary)
                            }
                        }
                    }

                    val filteredParties = if (partySearchQuery.isEmpty()) {
                        data.recentParties
                    } else {
                        data.recentParties.filter {
                            it.party.name.contains(partySearchQuery, ignoreCase = true) ||
                            it.party.phone?.contains(partySearchQuery, ignoreCase = true) == true
                        }
                    }

                    if (filteredParties.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No parties found", fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        items(filteredParties) { partyBalance ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.PartyDetail.createRoute(partyBalance.party.id)) }
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            partyBalance.party.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val dateStr = try {
                                            val invoices = data.recentInvoices.filter { it.partyId == partyBalance.party.id }
                                            if (invoices.isNotEmpty()) {
                                                java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date(invoices.maxByOrNull { it.invoiceDate }?.invoiceDate ?: System.currentTimeMillis()))
                                            } else ""
                                        } catch (_: Exception) { "" }
                                        if (dateStr.isNotBlank()) {
                                            Text(dateStr, fontSize = 12.sp, color = TextSecondary)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", kotlin.math.abs(partyBalance.balance)),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (partyBalance.isReceivable) GreenBalance else RedAccent
                                        )
                                        Text(
                                            if (partyBalance.isReceivable) "You'll Get" else "You'll Give",
                                            fontSize = 12.sp,
                                            color = if (partyBalance.isReceivable) GreenBalance else RedAccent
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // Transactions Tab
                if (selectedTab == 1) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = transactionSearchQuery,
                                onValueChange = { transactionSearchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("SEARCH TRANSACTIONS", fontSize = 13.sp, color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = TextSecondary)
                            }
                        }
                    }

                    if (data.recentInvoices.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No transactions yet", fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        items(data.recentInvoices) { invoice ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id)) }
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Invoice #${invoice.invoiceNumber.takeLast(4)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            java.text.SimpleDateFormat("dd MMM", java.util.Locale.US).format(java.util.Date(invoice.invoiceDate)),
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "#${invoice.invoiceNumber.takeLast(4)}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(GreenBalance.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("SALE", fontSize = 10.sp, color = GreenBalance, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Balance", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount - invoice.amountPaid),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.Print, contentDescription = "Print", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // Items Tab
                if (selectedTab == 2) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = itemSearchQuery,
                                onValueChange = { itemSearchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("SEARCH ITEMS", fontSize = 13.sp, color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = TextSecondary)
                            }
                            TextButton(onClick = { navController.navigate(Screen.AddItem.route) }) {
                                Text("+ New Item", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextSecondary)
                            }
                        }
                    }

                    // Manufacturing Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Inventory, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Introducing Manufacturing", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Manage goods by creating Bill of materials.", fontSize = 12.sp, color = TextSecondary)
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Items List
                    val filteredItems = if (itemSearchQuery.isEmpty()) {
                        data.recentItems
                    } else {
                        data.recentItems.filter {
                            it.name.contains(itemSearchQuery, ignoreCase = true) ||
                            it.hsnCode?.contains(itemSearchQuery, ignoreCase = true) == true
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No items found", fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        items(filteredItems) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.ItemDetail.createRoute(item.id)) }
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Sale Price", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", item.salePrice),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Purchase Price", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", item.purchasePrice),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("In Stock", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            String.format(Locale.US, "%.1f", item.stockQuantity),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.stockQuantity < 0) RedAccent else TextPrimary
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showTransactionSheet) {
        TransactionTypeSheet(
            onDismiss = { showTransactionSheet = false },
            onSelect = { transactionType ->
                when (transactionType) {
                    "sale_invoice", "credit_note", "sale_order", "estimate", "delivery_challan", "mobile_pos" -> {
                        navController.navigate(Screen.CreateInvoice.route)
                    }
                    "payment_in" -> navController.navigate(Screen.CashBank.route)
                    "purchase", "debit_note", "purchase_order" -> {
                        navController.navigate(Screen.Purchases.route)
                    }
                    "payment_out" -> navController.navigate(Screen.CashBank.route)
                    "expense" -> navController.navigate(Screen.Expenses.route)
                    "party_transfer" -> navController.navigate(Screen.CashBank.route)
                }
            }
        )
    }
}

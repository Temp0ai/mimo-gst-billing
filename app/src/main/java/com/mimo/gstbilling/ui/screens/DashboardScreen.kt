package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.navigation.VyaparBottomBar
import com.mimo.gstbilling.ui.navigation.VyaparDashboardBottomBar
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.utils.ShimmerDashboardSummary
import com.mimo.gstbilling.ui.utils.ShimmerTabRow
import com.mimo.gstbilling.ui.utils.ShimmerSearchBar
import com.mimo.gstbilling.ui.utils.ShimmerListItem
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
    val context = LocalContext.current
    val data by viewModel.data.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var expandedSection by remember { mutableStateOf("") }
    var showTransactionSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var partySearchQuery by remember { mutableStateOf("") }
    var transactionSearchQuery by remember { mutableStateOf("") }
    var itemSearchQuery by remember { mutableStateOf("") }
    var showPartyOptionsMenu by remember { mutableStateOf(false) }
    var showTransactionFilterDialog by remember { mutableStateOf(false) }
    var showInvoiceOptionsMenu by remember { mutableStateOf(false) }
    var showItemFilterDialog by remember { mutableStateOf(false) }
    var showItemOptionsMenu by remember { mutableStateOf(false) }
    var showManufacturingBanner by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    val menuItems = listOf(
        DrawerMenuItem("Parties", Icons.Filled.Group, hasExpand = true, subItems = listOf("All Parties", "Party Groups", "Party Statement")),
        DrawerMenuItem("Items", Icons.Filled.FormatListBulleted),
        DrawerMenuItem("Business Dashboard", Icons.Filled.Category),
        DrawerMenuItem("Reports", Icons.Filled.PieChart),
        DrawerMenuItem("Sale", Icons.Filled.FormatListBulleted, hasExpand = true, subItems = listOf("All Sales", "Create Sale", "Invoice Templates", "Credit Notes")),
        DrawerMenuItem("Purchase", Icons.Filled.ShoppingCart, hasExpand = true, subItems = listOf("All Purchases", "Create Purchase", "Debit Notes")),
        DrawerMenuItem("Delivery Challans", Icons.Filled.LocalShipping),
        DrawerMenuItem("Expense", Icons.Filled.Note, hasAddIcon = true),
        DrawerMenuItem("Cash & Bank", Icons.Filled.Store, hasExpand = true, subItems = listOf("Cash Book", "Bank Accounts")),
        DrawerMenuItem("Settings", Icons.Filled.Settings),
        DrawerMenuItem("Reports & Utilities", Icons.Filled.TrendingUp, hasExpand = true, subItems = listOf("Barcode Scanner", "Thermal Printer", "Import Data", "Vyapar Import")),
        DrawerMenuItem("Sync & Backup", Icons.Filled.CloudSync, hasExpand = true, subItems = listOf("Auto Backup", "Backup to phone", "Backup to e-mail", "Restore backup")),
        DrawerMenuItem("Online Store", Icons.Filled.Store, hasExpand = true, subItems = listOf("Store Settings", "Products")),
        DrawerMenuItem("Help & Support", Icons.Filled.Warning, hasExpand = true, subItems = listOf("FAQs", "Contact Support")),
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp).background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(data.companyName.take(1).uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(data.companyName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Business Account", fontSize = 12.sp, color = TextSecondary)
                    }
                    HorizontalDivider()
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
                                                "Delivery Challans" -> navController.navigate(Screen.DeliveryChallan.route)
                                                "Expense" -> navController.navigate(Screen.Expenses.route)
                                                "Settings" -> navController.navigate(Screen.Settings.route)
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
                                                    "Party Statement" -> navController.navigate(Screen.Parties.route)
                                                    "All Sales" -> navController.navigate(Screen.Sales.route)
                                                    "Create Sale" -> navController.navigate(Screen.CreateInvoice.route)
                                                    "Invoice Templates" -> navController.navigate(Screen.InvoiceTemplates.route)
                                                    "Credit Notes" -> navController.navigate(Screen.CreditNote.route)
                                                    "All Purchases" -> navController.navigate(Screen.Purchases.route)
                                                    "Create Purchase" -> navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "purchase"))
                                                    "Debit Notes" -> navController.navigate(Screen.DebitNote.route)
                                                    "Cash Book" -> navController.navigate(Screen.CashBook.route)
                                                    "Bank Accounts" -> navController.navigate(Screen.BankAccounts.route)
                                                    "Store Settings" -> navController.navigate(Screen.StoreManagement.route)
                                                    "Products" -> navController.navigate(Screen.Items.route)
                                                    // TODO: Each backup sub-item should navigate to its own dedicated screen
                                                    "Auto Backup" -> navController.navigate(Screen.BackupRestore.route)
                                                    "Backup to phone" -> navController.navigate(Screen.BackupRestore.route)
                                                    "Backup to e-mail" -> navController.navigate(Screen.ExportData.route)
                                                    "Restore backup" -> navController.navigate(Screen.BackupRestore.route)
                                                    "Barcode Scanner" -> navController.navigate(Screen.BarcodeScanner.route)
                                                    "Thermal Printer" -> navController.navigate(Screen.ThermalPrinter.route)
                                                    "Import Data" -> navController.navigate(Screen.ImportData.route)
                                                    "Vyapar Import" -> navController.navigate(Screen.VyaparDataImport.route)
                                                    "FAQs" -> navController.navigate(Screen.About.route)
                                                    "Contact Support" -> navController.navigate(Screen.About.route)
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
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            data.companyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.PaymentReminders.route) }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }
                        IconButton(onClick = {
                            val shareText = buildString {
                                append("Business: ${data.companyName}\n")
                                append("You'll Get: ₹${String.format(Locale.US, "%,d", data.pendingReceivables.toLong())}\n")
                                append("Sales (${java.text.SimpleDateFormat("MMM", java.util.Locale.US).format(java.util.Date())}): ₹${String.format(Locale.US, "%,d", data.totalSales.toLong())}\n")
                            }
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                setPackage("com.whatsapp")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = RedAccent, modifier = Modifier.size(24.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                VyaparDashboardBottomBar(
                    onTakePayment = { navController.navigate(Screen.CashBank.route) },
                    onAddClick = { showTransactionSheet = true },
                    onAddSale = { navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "sales")) }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(VyaparBackground),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (isLoading) {
                    item { ShimmerDashboardSummary() }
                    item { ShimmerTabRow() }
                    item { ShimmerSearchBar() }
                    items(5) { ShimmerListItem() }
                } else {
                // Two Summary Cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // You'll Get Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = VyaparGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("You'll Get", fontSize = 13.sp, color = VyaparTextSecondary, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "\u20B9${String.format(Locale.US, "%,d", data.pendingReceivables.toLong())}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VyaparTextPrimary
                                    )
                                    Text(
                                        text = ".${String.format(Locale.US, "%02d", ((data.pendingReceivables % 1) * 100).toInt())}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VyaparTextSecondary
                                    )
                                }
                            }
                        }
                        // Sale (Month) Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Category,
                                        contentDescription = null,
                                        tint = VyaparOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Sale (${java.text.SimpleDateFormat("MMM", java.util.Locale.US).format(java.util.Date())})",
                                        fontSize = 13.sp,
                                        color = VyaparTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\u20B9${String.format(Locale.US, "%,d", data.totalSales.toLong())}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VyaparTextPrimary
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
                            label = { Text("Parties", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp) },
                            shape = RoundedCornerShape(25.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparSelectedBg,
                                selectedLabelColor = VyaparRed,
                                containerColor = VyaparWhite,
                                labelColor = VyaparTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedTab == 0) VyaparRed else VyaparDivider,
                                selectedBorderColor = VyaparRed,
                                enabled = true,
                                selected = selectedTab == 0
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Transactions", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp) },
                            shape = RoundedCornerShape(25.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparSelectedBg,
                                selectedLabelColor = VyaparRed,
                                containerColor = VyaparWhite,
                                labelColor = VyaparTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedTab == 1) VyaparRed else VyaparDivider,
                                selectedBorderColor = VyaparRed,
                                enabled = true,
                                selected = selectedTab == 1
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Items", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp) },
                            shape = RoundedCornerShape(25.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparSelectedBg,
                                selectedLabelColor = VyaparRed,
                                containerColor = VyaparWhite,
                                labelColor = VyaparTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedTab == 2) VyaparRed else VyaparDivider,
                                selectedBorderColor = VyaparRed,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = partySearchQuery,
                                onValueChange = { partySearchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("SEARCH PARTY", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            Text(
                                "+ New Party",
                                color = VyaparBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clickable { navController.navigate(Screen.AddParty.route) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Box {
                                IconButton(onClick = { showPartyOptionsMenu = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = VyaparTextSecondary)
                                }
                                DropdownMenu(expanded = showPartyOptionsMenu, onDismissRequest = { showPartyOptionsMenu = false }) {
                                    DropdownMenuItem(text = { Text("Edit Party") }, onClick = { showPartyOptionsMenu = false; navController.navigate(Screen.AddParty.route) })
                                    DropdownMenuItem(text = { Text("WhatsApp") }, onClick = {
                                        showPartyOptionsMenu = false
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; setPackage("com.whatsapp") }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                                    })
                                    DropdownMenuItem(text = { Text("View Statement") }, onClick = { showPartyOptionsMenu = false; navController.navigate(Screen.Parties.route) })
                                }
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
                                Icon(Icons.Filled.Group, contentDescription = null, tint = VyaparEmptyStateIcon, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No parties yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap + to add a party", fontSize = 13.sp, color = VyaparTextSecondary)
                            }
                        }
                    } else {
                        items(filteredParties) { partyBalance ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.PartyDetail.createRoute(partyBalance.party.id)) }
                                    .padding(horizontal = 16.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            partyBalance.party.name,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VyaparTextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        val dateStr = try {
                                            val invoices = data.recentInvoices.filter { it.partyId == partyBalance.party.id }
                                            if (invoices.isNotEmpty()) {
                                                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US)
                                                sdf.format(java.util.Date(invoices.maxByOrNull { it.invoiceDate }?.invoiceDate ?: System.currentTimeMillis()))
                                            } else ""
                                        } catch (_: Exception) { "" }
                                        if (dateStr.isNotBlank()) {
                                            Text(dateStr, fontSize = 13.sp, color = VyaparTextSecondary)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", kotlin.math.abs(partyBalance.balance)),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (partyBalance.isReceivable) VyaparGreen else VyaparRed
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            if (partyBalance.isReceivable) "You'll Get" else "You'll Give",
                                            fontSize = 12.sp,
                                            color = if (partyBalance.isReceivable) VyaparGreen else VyaparRed
                                        )
                                    }
                                }
                                HorizontalDivider(color = VyaparDivider, thickness = 0.5.dp)
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
                            Icon(Icons.Filled.Search, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = transactionSearchQuery,
                                onValueChange = { transactionSearchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("SEARCH TRANSACTIONS", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            IconButton(onClick = { showTransactionFilterDialog = true }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = VyaparTextSecondary)
                            }
                        }
                    }

                    if (data.recentInvoices.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Receipt, contentDescription = null, tint = VyaparEmptyStateIcon, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No transactions yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap + to create a transaction", fontSize = 13.sp, color = VyaparTextSecondary)
                            }
                        }
                    } else {
                        items(data.recentInvoices) { invoice ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id)) }
                                    .padding(horizontal = 16.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Invoice #${invoice.invoiceNumber.takeLast(4)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            java.text.SimpleDateFormat("dd MMM", java.util.Locale.US).format(java.util.Date(invoice.invoiceDate)),
                                            fontSize = 12.sp,
                                            color = VyaparTextSecondary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "#${invoice.invoiceNumber.takeLast(4)}",
                                            fontSize = 12.sp,
                                            color = VyaparTextSecondary
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(VyaparGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("SALE", fontSize = 10.sp, color = VyaparGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total", fontSize = 12.sp, color = VyaparTextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VyaparTextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Balance", fontSize = 12.sp, color = VyaparTextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount - invoice.amountPaid),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VyaparTextPrimary
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(onClick = {
                                            scope.launch {
                                                try {
                                                    val pdfFile = java.io.File(context.cacheDir, "invoice_${invoice.id}.pdf")
                                                    val outputStream = pdfFile.outputStream()
                                                    outputStream.write("Invoice #${invoice.invoiceNumber}\nDate: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date(invoice.invoiceDate))}\nTotal: ₹${String.format(Locale.US, "%,.2f", invoice.totalAmount)}\nPaid: ₹${String.format(Locale.US, "%,.2f", invoice.amountPaid)}\n".toByteArray())
                                                    outputStream.close()
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                        type = "application/pdf"
                                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(android.content.Intent.createChooser(intent, "Print Invoice"))
                                                } catch (e: Exception) {
                                                    snackbarHostState.showSnackbar("Unable to generate PDF")
                                                }
                                            }
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.Print, contentDescription = "Print", tint = VyaparTextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = {
                                            val shareText = "Invoice #${invoice.invoiceNumber} - ₹${String.format(Locale.US, "%,.2f", invoice.totalAmount)}"
                                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                                setPackage("com.whatsapp")
                                            }
                                            context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = VyaparTextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        Box {
                                            IconButton(onClick = { showInvoiceOptionsMenu = true }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = VyaparTextSecondary, modifier = Modifier.size(18.dp))
                                            }
                                            DropdownMenu(expanded = showInvoiceOptionsMenu, onDismissRequest = { showInvoiceOptionsMenu = false }) {
                                                DropdownMenuItem(text = { Text("Edit Invoice") }, onClick = { showInvoiceOptionsMenu = false; navController.navigate(Screen.EditInvoice.createRoute(invoice.id)) })
                                                DropdownMenuItem(text = { Text("WhatsApp") }, onClick = {
                                                    showInvoiceOptionsMenu = false
                                                    val shareText = "Invoice #${invoice.invoiceNumber} - ₹${String.format(Locale.US, "%,.2f", invoice.totalAmount)}"
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, shareText); setPackage("com.whatsapp") }
                                                    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                                                })
                                                DropdownMenuItem(text = { Text("Delete") }, onClick = { showInvoiceOptionsMenu = false; scope.launch { snackbarHostState.showSnackbar("Invoice deleted") } })
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(color = VyaparDivider, thickness = 0.5.dp)
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
                            Icon(Icons.Filled.Search, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = itemSearchQuery,
                                onValueChange = { itemSearchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("SEARCH ITEMS", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                            IconButton(onClick = { showItemFilterDialog = true }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = VyaparTextSecondary)
                            }
                            TextButton(onClick = { navController.navigate(Screen.AddItem.route) }) {
                                Text("+ New Item", color = VyaparBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Box {
                                IconButton(onClick = { showItemOptionsMenu = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = VyaparTextSecondary)
                                }
                                DropdownMenu(expanded = showItemOptionsMenu, onDismissRequest = { showItemOptionsMenu = false }) {
                                    DropdownMenuItem(text = { Text("Edit Item") }, onClick = { showItemOptionsMenu = false; navController.navigate(Screen.AddItem.route) })
                                    DropdownMenuItem(text = { Text("Share") }, onClick = {
                                        showItemOptionsMenu = false
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; setPackage("com.whatsapp") }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                                    })
                                    DropdownMenuItem(text = { Text("Delete") }, onClick = { showItemOptionsMenu = false; scope.launch { snackbarHostState.showSnackbar("Item deleted") } })
                                }
                            }
                        }
                    }

                    // Manufacturing Banner
                    if (showManufacturingBanner) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = VyaparInfoBackground)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Inventory, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Introducing Manufacturing", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                                    Text("Manage goods by creating Bill of materials.", fontSize = 12.sp, color = VyaparTextSecondary)
                                }
                                IconButton(onClick = { showManufacturingBanner = false }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Close", tint = VyaparTextSecondary, modifier = Modifier.size(16.dp))
                                }
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
                                Icon(Icons.Filled.FormatListBulleted, contentDescription = null, tint = VyaparEmptyStateIcon, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No items yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap + to add an item", fontSize = 13.sp, color = VyaparTextSecondary)
                            }
                        }
                    } else {
                        items(filteredItems) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(Screen.ItemDetail.createRoute(item.id)) }
                                    .padding(horizontal = 16.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VyaparTextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = {
                                        val shareText = "${item.name} - ₹${String.format(Locale.US, "%,.2f", item.salePrice)}"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                            setPackage("com.whatsapp")
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = VyaparTextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Sale Price", fontSize = 12.sp, color = VyaparTextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", item.salePrice),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VyaparTextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Purchase Price", fontSize = 12.sp, color = VyaparTextSecondary)
                                        Text(
                                            String.format(Locale.US, "\u20B9%,.2f", item.purchasePrice),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VyaparTextPrimary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("In Stock", fontSize = 12.sp, color = VyaparTextSecondary)
                                        Text(
                                            String.format(Locale.US, "%.1f", item.stockQuantity),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.stockQuantity < 0) VyaparRed else VyaparTextPrimary
                                        )
                                    }
                                }
                                HorizontalDivider(color = VyaparDivider, thickness = 0.5.dp)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showTransactionSheet) {
        TransactionTypeSheet(
            onDismiss = { showTransactionSheet = false },
            onSelect = { transactionType ->
                when (transactionType) {
                    "sale_invoice", "sale_order", "estimate", "mobile_pos" -> {
                        navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "sales"))
                    }
                    "credit_note" -> navController.navigate(Screen.CreditNote.route)
                    "delivery_challan" -> navController.navigate(Screen.DeliveryChallan.route)
                    "payment_in" -> navController.navigate(Screen.CashBank.route)
                    "purchase" -> navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "purchase"))
                    "debit_note" -> navController.navigate(Screen.DebitNote.route)
                    "purchase_order" -> navController.navigate(Screen.Orders.route)
                    "payment_out" -> navController.navigate(Screen.CashBank.route)
                    "expense" -> navController.navigate(Screen.Expenses.route)
                    "party_transfer" -> navController.navigate(Screen.CashBank.route)
                }
            }
        )
    }

    if (showTransactionFilterDialog) {
        AlertDialog(
            onDismissRequest = { showTransactionFilterDialog = false },
            title = { Text("Filter Transactions", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Sales", "Purchases", "Payments", "Expenses").forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showTransactionFilterDialog = false; scope.launch { snackbarHostState.showSnackbar("Filtered: $type") } }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Circle, contentDescription = null, tint = Primary, modifier = Modifier.size(8.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(type, fontSize = 14.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTransactionFilterDialog = false }) { Text("Close") } }
        )
    }

    if (showItemFilterDialog) {
        AlertDialog(
            onDismissRequest = { showItemFilterDialog = false },
            title = { Text("Filter Items", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All Items", "In Stock", "Low Stock", "Out of Stock", "With Tax", "Without Tax").forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showItemFilterDialog = false; scope.launch { snackbarHostState.showSnackbar("Filtered: $type") } }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Circle, contentDescription = null, tint = Primary, modifier = Modifier.size(8.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(type, fontSize = 14.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showItemFilterDialog = false }) { Text("Close") } }
        )
    }
}

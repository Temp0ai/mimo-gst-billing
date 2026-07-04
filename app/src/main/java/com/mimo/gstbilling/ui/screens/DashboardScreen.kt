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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    val hasNewBadge: Boolean = false
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

    val menuItems = listOf(
        DrawerMenuItem("Business Profile", Icons.Filled.Business),
        DrawerMenuItem("Parties", Icons.Filled.Group, hasExpand = true, subItems = listOf("All Parties", "Party Groups", "Party Statement")),
        DrawerMenuItem("Items", Icons.Filled.Inventory, hasExpand = true, subItems = listOf("All Items", "Products", "Services", "Batch/Serial")),
        DrawerMenuItem("Sale", Icons.Filled.TrendingUp, hasExpand = true, subItems = listOf("All Sales", "Create Sale")),
        DrawerMenuItem("Purchase", Icons.Filled.LocalShipping, hasExpand = true, subItems = listOf("All Purchases", "Create Purchase")),
        DrawerMenuItem("Orders", Icons.Filled.Description, hasExpand = true, subItems = listOf("Estimates", "Quotations", "Orders")),
        DrawerMenuItem("Expense", Icons.Filled.Receipt),
        DrawerMenuItem("Cash & Bank", Icons.Filled.AccountBalance),
        DrawerMenuItem("Reports", Icons.Filled.PieChart, hasExpand = true, subItems = listOf("GSTR-1", "GSTR-3B", "Day Book", "Cash Flow", "Balance Sheet", "P&L", "Expense Categories")),
        DrawerMenuItem("Manufacturing", Icons.Filled.Build),
        DrawerMenuItem("Store Management", Icons.Filled.Store),
        DrawerMenuItem("Stock Transfer", Icons.Filled.LocalShipping),
        DrawerMenuItem("Barcode Scanner", Icons.Filled.CameraAlt),
        DrawerMenuItem("Thermal Printer", Icons.Filled.Print),
        DrawerMenuItem("Payment Reminders", Icons.Filled.Notifications),
        DrawerMenuItem("Import Data", Icons.Filled.Warning),
        DrawerMenuItem("Backup/Restore", Icons.Filled.Warning),
        DrawerMenuItem("Settings", Icons.Filled.Settings)
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
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = data.companyName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GST Registered Business",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
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
                                            "Business Profile" -> navController.navigate(Screen.BusinessProfile.route)
                                            "Parties" -> navController.navigate(Screen.Parties.route)
                                            "Items" -> navController.navigate(Screen.Items.route)
                                            "Reports" -> navController.navigate(Screen.Reports.route)
                                            "Sale" -> navController.navigate(Screen.Sales.route)
                                            "Purchase" -> navController.navigate(Screen.Purchases.route)
                                            "Expense" -> navController.navigate(Screen.Expenses.route)
                                            "Cash & Bank" -> navController.navigate(Screen.CashBank.route)
                                            "Settings" -> navController.navigate(Screen.Settings.route)
                                            "Manufacturing" -> navController.navigate(Screen.Manufacturing.route)
                                            "Store Management" -> navController.navigate(Screen.StoreManagement.route)
                                            "Barcode Scanner" -> navController.navigate(Screen.BarcodeScanner.route)
                                            "Stock Transfer" -> navController.navigate(Screen.StockTransfer.route)
                                            "Orders" -> navController.navigate(Screen.Orders.route)
                                            "Payment Reminders" -> navController.navigate(Screen.PaymentReminders.route)
                                            "Import Data" -> navController.navigate(Screen.ImportData.route)
                                            "Thermal Printer" -> navController.navigate(Screen.ThermalPrinter.route)
                                            "Backup/Restore" -> navController.navigate(Screen.BackupRestore.route)
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(item.title, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            if (item.hasExpand) {
                                Icon(if (expandedSection == item.title) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
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
                                                "All Items" -> navController.navigate(Screen.Items.route)
                                                "Products" -> navController.navigate(Screen.Items.route)
                                                "Services" -> navController.navigate(Screen.Items.route)
                                                "Batch/Serial" -> navController.navigate(Screen.ItemBatchTracking.route)
                                                "All Sales" -> navController.navigate(Screen.Sales.route)
                                                "Create Sale" -> navController.navigate(Screen.CreateInvoice.route)
                                                "All Purchases" -> navController.navigate(Screen.Purchases.route)
                                                "Create Purchase" -> navController.navigate(Screen.CreateInvoice.route)
                                                "Estimates" -> navController.navigate(Screen.Orders.route)
                                                "Quotations" -> navController.navigate(Screen.Orders.route)
                                                "Orders" -> navController.navigate(Screen.Orders.route)
                                                "GSTR-1" -> navController.navigate(Screen.Gstr1Report.route)
                                                "GSTR-3B" -> navController.navigate(Screen.Gstr3bReport.route)
                                                "Day Book" -> navController.navigate(Screen.DayBookReport.route)
                                                "Cash Flow" -> navController.navigate(Screen.CashFlowReport.route)
                                                "Balance Sheet" -> navController.navigate(Screen.BalanceSheet.route)
                                                "P&L" -> navController.navigate(Screen.ProfitLossReport.route)
                                                "Expense Categories" -> navController.navigate(Screen.ExpenseCategoryReport.route)
                                            }
                                        }
                                        .padding(start = 52.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Circle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(6.dp))
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
                    title = { Text(data.companyName, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) { Icon(Icons.Filled.Notifications, contentDescription = "Notifications") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.CashBank.route) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Text("Take Payment", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { navController.navigate(Screen.CreateInvoice.route) },
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { navController.navigate(Screen.CreateInvoice.route) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) {
                        Text("Add Sale", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(LightBlueBg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "\u20B9${String.format(Locale.US, "%,.2f", data.pendingReceivables)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Text("Total Sales", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("\u20B9${String.format(Locale.US, "%,.0f", data.totalSales)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Text("Total Purchase", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("\u20B9${String.format(Locale.US, "%,.0f", data.totalPurchases)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Text("You'll Give", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("\u20B9${String.format(Locale.US, "%,.0f", data.pendingPayables)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Text("Expenses", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("\u20B9${String.format(Locale.US, "%,.0f", data.totalExpenses)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                            }
                        }
                    }
                }

                if (data.recentParties.isNotEmpty()) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Recent Parties", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("See All", fontSize = 13.sp, color = Primary, modifier = Modifier.clickable { navController.navigate(Screen.Parties.route) })
                        }
                    }
                    items(data.recentParties) { party ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.PartyDetail.createRoute(party.id)) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Business, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(party.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text(party.phone ?: "", fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(String.format(Locale.US, "\u20B9%,.2f", party.balance), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (party.balance > 0) GreenBalance else RedAccent)
                                    Text("You'll Get", fontSize = 11.sp, color = GreenBalance)
                                }
                            }
                        }
                    }
                }

                if (data.recentInvoices.isNotEmpty()) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Recent Invoices", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("See All", fontSize = 13.sp, color = Primary, modifier = Modifier.clickable { navController.navigate(Screen.Sales.route) })
                        }
                    }
                    items(data.recentInvoices) { invoice ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id)) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(invoice.invoiceNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date(invoice.invoiceDate)), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(String.format(Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(invoice.paymentStatus.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = if (invoice.paymentStatus == "paid") GreenBalance else RedAccent)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

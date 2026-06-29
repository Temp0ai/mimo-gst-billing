package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mimo GST Billing", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BlueHeader),
                    navigationIcon = {
                        IconButton(onClick = { /* open drawer */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateInvoice.route) },
                    containerColor = RedAccent,
                    contentColor = OnPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Sale")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)
            ) {
                item { SummarySection() }
                item { QuickActionsSection(navController) }
                item { RecentPartiesSection() }
            }
        }
    }
}

@Composable
private fun DrawerContent(navController: NavController, drawerState: DrawerState) {
    var expandedParties by remember { mutableStateOf(false) }
    var expandedSale by remember { mutableStateOf(false) }
    var expandedPurchase by remember { mutableStateOf(false) }
    var expandedCashBank by remember { mutableStateOf(false) }

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxSize()) {
            // Company Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueHeader)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            tint = OnPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Arihant Enterprises",
                                color = OnPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "enterprises.arihant87@gmail.c...",
                                color = OnPrimary.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Change Company",
                            color = OnPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = OnPrimary
                        )
                    }
                }
            }

            // Menu Items
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.People, contentDescription = null) },
                label = { Text("Parties") },
                badge = { Badge(containerColor = RedAccent) { Text("NEW") } },
                selected = false,
                onClick = { expandedParties = !expandedParties }
            )

            if (expandedParties) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    label = { Text("Party Details") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Parties.route) }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                    label = { Text("Loyalty Points") },
                    badge = { Badge(containerColor = RedAccent) { Text("NEW") } },
                    selected = false,
                    onClick = { }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.NetworkCheck, contentDescription = null) },
                    label = { Text("Vyapar Network") },
                    selected = false,
                    onClick = { }
                )
            }

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.List, contentDescription = null) },
                label = { Text("Items") },
                selected = false,
                onClick = { navController.navigate(Screen.Items.route) }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                label = { Text("Business Dashboard") },
                selected = false,
                onClick = { }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                label = { Text("Reports") },
                selected = false,
                onClick = { navController.navigate(Screen.Reports.route) }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                label = { Text("Sale") },
                selected = false,
                onClick = { expandedSale = !expandedSale }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                label = { Text("Purchase") },
                selected = false,
                onClick = { expandedPurchase = !expandedPurchase }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                label = { Text("Expense") },
                selected = false,
                onClick = { }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                label = { Text("Cash & Bank") },
                selected = false,
                onClick = { expandedCashBank = !expandedCashBank }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Store, contentDescription = null) },
                label = { Text("My Online Store") },
                selected = false,
                onClick = { }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Settings") },
                badge = { Badge(containerColor = RedAccent) { Text("NEW") } },
                selected = false,
                onClick = { navController.navigate(Screen.Settings.route) }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                label = { Text("Backup/Restore") },
                selected = false,
                onClick = { }
            )
        }
    }
}

@Composable
private fun SummarySection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Business Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryCard(
                title = "Total Sales",
                amount = "Rs. 1,24,500",
                color = Success,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            SummaryCard(
                title = "Total Purchase",
                amount = "Rs. 45,200",
                color = Error,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryCard(
                title = "Receivable",
                amount = "Rs. 34,800",
                color = GreenBalance,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            SummaryCard(
                title = "Payable",
                amount = "Rs. 12,300",
                color = RedAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun QuickActionsSection(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionButton("Sale", Icons.Default.Receipt, RedAccent) {
                navController.navigate(Screen.CreateInvoice.route)
            }
            ActionButton("Purchase", Icons.Default.ShoppingCart, Primary) { }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionButton("Parties", Icons.Default.People, Success) {
                navController.navigate(Screen.Parties.route)
            }
            ActionButton("Items", Icons.Default.Inventory, Warning) {
                navController.navigate(Screen.Items.route)
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 160.dp, height = 80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = text, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
    }
}

@Composable
private fun RecentPartiesSection() {
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Recent Parties",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            PartyRow("dignitary defence academy", "Rs. 22,570", "19 Jun 26")
            PartyRow("Sha Khimji & Premji Co", "Rs. 21,100", "02 Jun 26")
            PartyRow("Sanman enterprises", "Rs. 1,15,227", "09 May 26")
        }
    }
}

@Composable
private fun PartyRow(name: String, amount: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, fontWeight = FontWeight.Bold, color = GreenBalance)
            Text("You'll Get", style = MaterialTheme.typography.bodySmall, color = GreenBalance)
        }
    }
}

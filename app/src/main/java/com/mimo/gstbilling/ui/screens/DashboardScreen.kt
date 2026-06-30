package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bank
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.BlueHeader
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.Secondary
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val hasExpand: Boolean = false,
    val hasNewBadge: Boolean = false,
    val subItems: List<String> = emptyList()
)

data class PartySummary(
    val name: String,
    val balance: Double,
    val isPositive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var expandedSection by remember { mutableStateOf("") }

    val menuItems = listOf(
        DrawerMenuItem("Parties", Icons.Filled.Group, hasExpand = true, hasNewBadge = true, subItems = listOf("All Parties", "Receivable", "Payable")),
        DrawerMenuItem("Items", Icons.Filled.Inventory, hasExpand = true, subItems = listOf("All Items", "Products", "Services")),
        DrawerMenuItem("Business Dashboard", Icons.Filled.PieChart),
        DrawerMenuItem("Reports", Icons.Filled.Description),
        DrawerMenuItem("Sale", Icons.Filled.TrendingUp, hasExpand = true, subItems = listOf("All Sales", "Create Sale", "Sales Return")),
        DrawerMenuItem("Purchase", Icons.Filled.ShoppingCart, hasExpand = true, subItems = listOf("All Purchases", "Create Purchase")),
        DrawerMenuItem("Expense", Icons.Filled.Receipt),
        DrawerMenuItem("Cash & Bank", Icons.Filled.Bank),
        DrawerMenuItem("Settings", Icons.Filled.Settings, hasNewBadge = true),
        DrawerMenuItem("Backup/Restore", Icons.Filled.Warning)
    )

    val recentParties = listOf(
        PartySummary("Dignitary Defence Academy", 22570.0),
        PartySummary("Sha Khimji & Premji Co", 21100.0),
        PartySummary("Sanman Enterprises", 115227.0),
        PartySummary("Adv. Pramod Bendre", 36640.0),
        PartySummary("Agad Logistics", 6380.0),
        PartySummary("Ishwar Heart Clinic", 370.0),
        PartySummary("Taste of Irani", 7350.0)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlueHeader)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Dignitary Defence Academy",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "dignitarydefence@gmail.com",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Change Company",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
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
                                            "Parties" -> navController.navigate(Screen.Parties.route)
                                            "Items" -> navController.navigate(Screen.Items.route)
                                            "Reports" -> navController.navigate(Screen.Reports.route)
                                            "Sale" -> navController.navigate(Screen.Sales.route)
                                            "Purchase" -> navController.navigate(Screen.Purchases.route)
                                            "Expense" -> navController.navigate(Screen.Expenses.route)
                                            "Cash & Bank" -> navController.navigate(Screen.CashBank.route)
                                            "Settings" -> navController.navigate(Screen.Settings.route)
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = item.title,
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            if (item.hasNewBadge) {
                                Box(
                                    modifier = Modifier
                                        .background(RedAccent, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("NEW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (item.hasExpand) {
                                Icon(
                                    imageVector = if (expandedSection == item.title) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (item.hasExpand && expandedSection == item.title) {
                            item.subItems.forEach { subItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(start = 52.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Circle,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(6.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = subItem,
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
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
                        Text("Dashboard", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.CreateInvoice.route) },
                    containerColor = RedAccent,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Sale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Business Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Sales",
                            amount = "2,85,430",
                            color = Primary
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Purchase",
                            amount = "1,42,200",
                            color = RedAccent
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Receivable",
                            amount = "2,09,637",
                            color = GreenBalance,
                            subtitle = "You'll Get"
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Payable",
                            amount = "78,500",
                            color = RedAccent,
                            subtitle = "You'll Pay"
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            title = "Sale",
                            icon = Icons.Filled.TrendingUp,
                            color = Primary
                        ) {
                            navController.navigate(Screen.CreateInvoice.route)
                        }
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            title = "Purchase",
                            icon = Icons.Filled.ShoppingCart,
                            color = RedAccent
                        ) {
                            navController.navigate(Screen.Purchases.route)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            title = "Parties",
                            icon = Icons.Filled.People,
                            color = GreenBalance
                        ) {
                            navController.navigate(Screen.Parties.route)
                        }
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            title = "Items",
                            icon = Icons.Filled.Inventory,
                            color = Color(0xFFFF9800)
                        ) {
                            navController.navigate(Screen.Items.route)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recent Parties",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(recentParties) { party ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.Parties.route)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Business,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = party.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "You'll Get",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = String.format("Rs.%,.0f", party.balance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenBalance
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    color: Color,
    subtitle: String = ""
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rs." + amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = color,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

package com.mimo.gstbilling.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.navigation.VyaparBottomBar
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(navController: NavController, viewModel: ItemViewModel = hiltViewModel()) {
    val items by viewModel.allItems.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("PRODUCTS", "SERVICES", "CATEGORIES", "UNITS")
    val scope = rememberCoroutineScope()
    var showMoreOptions by remember { mutableStateOf(false) }

    val filteredItems = items.filter {
        it.name.contains(searchText, ignoreCase = true) && (selectedTab == 0 || (selectedTab == 1 && it.isService) || (selectedTab == 2 && false) || (selectedTab == 3 && false))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Items", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.ItemSettings.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showMoreOptions = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A),
                    actionIconContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        bottomBar = {
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
                        }
                    }
                )
            }
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val selectedTab = when (currentRoute) {
                Screen.Dashboard.route -> 0
                Screen.Parties.route -> 1
                Screen.Items.route -> 3
                Screen.Settings.route -> 4
                else -> 3
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
        ) {
            // Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) RedAccent else TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(40.dp)
                                        .background(if (isSelected) RedAccent else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search Items by Name or Code", fontSize = 14.sp) },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )
            }

            // Item List
            items(filteredItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { navController.navigate(Screen.ItemDetail.createRoute(item.id)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Item Name
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }

                        // Sale Price
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Sale Price",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                String.format(java.util.Locale.US, "\u20B9%,.2f", item.salePrice),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        // Purchase Price
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Purchase Price",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                String.format(java.util.Locale.US, "\u20B9%,.2f", item.purchasePrice),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        // In Stock
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "In Stock",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                item.stockQuantity.toInt().toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.stockQuantity > 10) GreenBalance else RedAccent
                            )
                        }

                        // Share Icon
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable { /* Share */ },
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Empty State
            item {
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No items found",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // More Options Bottom Sheet
    if (showMoreOptions) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptions = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "More Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Mark Items as Active",
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMoreOptions = false }
                        .padding(vertical = 12.dp)
                )
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Text(
                    "Mark Items as Inactive",
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMoreOptions = false }
                        .padding(vertical = 12.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

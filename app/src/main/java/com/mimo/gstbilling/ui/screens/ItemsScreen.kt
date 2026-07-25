package com.mimo.gstbilling.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
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
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterType by remember { mutableIntStateOf(0) }
    var filterStock by remember { mutableIntStateOf(0) }
    var sortBy by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val filteredItems = items.filter {
        it.name.contains(searchText, ignoreCase = true) &&
        (selectedTab == 0 || (selectedTab == 1 && it.isService) || (selectedTab == 2 && false) || (selectedTab == 3 && false)) &&
        (filterType == 0 || (filterType == 1 && !it.isService) || (filterType == 2 && it.isService)) &&
        (filterStock == 0 || (filterStock == 1 && it.stockQuantity <= 5) || (filterStock == 2 && it.stockQuantity <= 0))
    }.let { list ->
        when (sortBy) {
            1 -> list.sortedBy { it.name }
            2 -> list.sortedBy { it.salePrice }
            3 -> list.sortedBy { it.stockQuantity }
            else -> list
        }
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
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showMoreOptions = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = VyaparTextPrimary,
                    actionIconContentColor = VyaparTextPrimary
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
                            "sale" -> navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "sales"))
                            "purchase" -> navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "purchase"))
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
                .background(VyaparBackground)
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
                                    color = if (isSelected) VyaparRed else VyaparTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(40.dp)
                                        .background(if (isSelected) VyaparRed else Color.Transparent)
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
                            tint = VyaparTextSecondary
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
                    colors = CardDefaults.cardColors(containerColor = VyaparWhite),
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
                                color = VyaparTextPrimary
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
                                color = VyaparTextSecondary
                            )
                            Text(
                                String.format(java.util.Locale.US, "\u20B9%,.2f", item.salePrice),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = VyaparTextPrimary
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
                                color = VyaparTextSecondary
                            )
                            Text(
                                String.format(java.util.Locale.US, "\u20B9%,.2f", item.purchasePrice),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = VyaparTextPrimary
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
                                color = VyaparTextSecondary
                            )
                            Text(
                                item.stockQuantity.toInt().toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.stockQuantity > 10) VyaparGreen else VyaparRed
                            )
                        }

                        // Share Icon
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable {
                                    val shareText = buildString {
                                        appendLine("Item: ${item.name}")
                                        appendLine("Sale Price: ${String.format(java.util.Locale.US, "\u20B9%,.2f", item.salePrice)}")
                                        appendLine("Purchase Price: ${String.format(java.util.Locale.US, "\u20B9%,.2f", item.purchasePrice)}")
                                        appendLine("In Stock: ${item.stockQuantity.toInt()}")
                                        appendLine("Type: ${if (item.isService) "Service" else "Product"}")
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        setPackage("com.whatsapp")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share item via"))
                                },
                            tint = VyaparTextSecondary
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
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Store,
                                contentDescription = null,
                                tint = VyaparEmptyStateIcon,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No items yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = VyaparTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap + to add an item",
                                fontSize = 13.sp,
                                color = VyaparTextSecondary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Filter Items",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyaparTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("Type", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = VyaparTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filterType == 0, onClick = { filterType = 0 }, label = { Text("All") })
                    FilterChip(selected = filterType == 1, onClick = { filterType = 1 }, label = { Text("Products") })
                    FilterChip(selected = filterType == 2, onClick = { filterType = 2 }, label = { Text("Services") })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Stock Status", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = VyaparTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filterStock == 0, onClick = { filterStock = 0 }, label = { Text("All") })
                    FilterChip(selected = filterStock == 1, onClick = { filterStock = 1 }, label = { Text("Low Stock") })
                    FilterChip(selected = filterStock == 2, onClick = { filterStock = 2 }, label = { Text("Out of Stock") })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Sort By", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = VyaparTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = sortBy == 0, onClick = { sortBy = 0 }, label = { Text("Default") })
                    FilterChip(selected = sortBy == 1, onClick = { sortBy = 1 }, label = { Text("Name") })
                    FilterChip(selected = sortBy == 2, onClick = { sortBy = 2 }, label = { Text("Price") })
                    FilterChip(selected = sortBy == 3, onClick = { sortBy = 3 }, label = { Text("Stock") })
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
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
                    color = VyaparTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Mark Items as Active",
                    fontSize = 15.sp,
                    color = VyaparTextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            filteredItems.forEach { item ->
                                if (!item.isActive) viewModel.toggleItemActive(item)
                            }
                            showMoreOptions = false
                        }
                        .padding(vertical = 12.dp)
                )
                HorizontalDivider(color = VyaparDivider)
                Text(
                    "Mark Items as Inactive",
                    fontSize = 15.sp,
                    color = VyaparTextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            filteredItems.forEach { item ->
                                if (item.isActive) viewModel.toggleItemActive(item)
                            }
                            showMoreOptions = false
                        }
                        .padding(vertical = 12.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

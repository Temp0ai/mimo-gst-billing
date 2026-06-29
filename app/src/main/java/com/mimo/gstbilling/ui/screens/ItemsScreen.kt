package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class Product(
    val name: String,
    val salePrice: String,
    val purchasePrice: String,
    val stock: String,
    val isLowStock: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("PRODUCTS", "SERVICES", "CATEGORIES", "UNITS")

    val products = remember {
        mutableStateListOf(
            Product("150ml R Cup", "2.20", "0.00", "3,000.00"),
            Product("2 option vending machine", "1,00,000.00", "0.00", "532.00"),
            Product("210ml R cup", "3,000.00", "0.00", "2.70", isLowStock = true),
            Product("3 lane machine", "11,800.00", "0.00", "2.00", isLowStock = true),
            Product("3 option vending machine", "12,500.00", "0.00", "12.00"),
            Product("4 option vending machine", "14,500.00", "0.00", "0.00", isLowStock = true)
        )
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
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlueHeader)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = RedAccent,
                contentColor = OnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Product")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Surface,
                contentColor = Primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Search Items by Name or Code") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Product List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(products.size) { index ->
                    ProductListItem(products[index])
                    if (index < products.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductListItem(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Sale Price", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("\u20B9 ${product.salePrice}", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Purchase Price", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("\u20B9 ${product.purchasePrice}", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("In Stock", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        product.stock,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isLowStock) Error else GreenBalance
                    )
                }
            }
        }
    }
}

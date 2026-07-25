package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewStoreScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "In Stock", "Out of Stock")
    val products = remember {
        listOf(
            CatalogueProduct(1, "Wireless Mouse", 599.0, 45),
            CatalogueProduct(2, "USB-C Cable", 299.0, 120),
            CatalogueProduct(3, "Notebook Set", 189.0, 80),
            CatalogueProduct(4, "Bluetooth Speaker", 1299.0, 25),
            CatalogueProduct(5, "Phone Case", 399.0, 200),
            CatalogueProduct(6, "Desk Lamp", 899.0, 15),
            CatalogueProduct(7, "Keyboard", 1599.0, 30),
            CatalogueProduct(8, "Webcam", 2499.0, 0)
        )
    }

    val filteredProducts = products.filter {
        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)) &&
                when (selectedFilter) {
                    "In Stock" -> it.stock > 0
                    "Out of Stock" -> it.stock == 0
                    else -> true
                }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Store", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Store", tint = Primary)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search store products...", color = VyaparSearchHint) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = VyaparIconDefault) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyaparInputFocused,
                        unfocusedBorderColor = VyaparInputBorder,
                        focusedContainerColor = VyaparWhite,
                        unfocusedContainerColor = VyaparWhite
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparLightBlue,
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = VyaparEmptyStateIcon,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No products in store", fontSize = 16.sp, color = VyaparEmptyStateText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add products to your catalogue to display them here", fontSize = 13.sp, color = VyaparEmptyStateText, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts) { product ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.CatalogueItemDetail.createRoute(product.id))
                            }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(LightBlueBg, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Inventory2,
                                        contentDescription = null,
                                        tint = Primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    if (product.stock == 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .background(RedAccent, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("SOLD OUT", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        product.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "\u20B9${String.format("%,.0f", product.price)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (product.stock > 0) "In Stock (${product.stock})" else "Out of Stock",
                                        fontSize = 11.sp,
                                        color = if (product.stock > 0) GreenBalance else RedAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

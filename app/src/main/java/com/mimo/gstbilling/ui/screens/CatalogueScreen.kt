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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

data class CatalogueProduct(
    val id: Long,
    val name: String,
    val price: Double,
    val stock: Int,
    val imageRes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var drawerState by remember { mutableStateOf(false) }

    val categories = listOf("All", "Electronics", "Clothing", "Groceries", "Furniture", "Accessories")
    val products = remember {
        listOf(
            CatalogueProduct(1, "Wireless Mouse", 599.0, 45),
            CatalogueProduct(2, "USB-C Cable", 299.0, 120),
            CatalogueProduct(3, "Notebook Set", 189.0, 80),
            CatalogueProduct(4, "Bluetooth Speaker", 1299.0, 25),
            CatalogueProduct(5, "Phone Case", 399.0, 200),
            CatalogueProduct(6, "Desk Lamp", 899.0, 15),
            CatalogueProduct(7, "Keyboard", 1599.0, 30),
            CatalogueProduct(8, "Webcam", 2499.0, 12)
        )
    }

    val filteredProducts = products.filter {
        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)) &&
                (selectedCategory == "All" || true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalogue", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.ViewStore.route) }) {
                        Icon(Icons.Filled.Store, contentDescription = "View Store", tint = Primary)
                    }
                    IconButton(onClick = { navController.navigate(Screen.StoreManagement.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Store Settings", tint = Primary)
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
                    placeholder = { Text("Search products...", color = VyaparSearchHint) },
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
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyaparLightBlue,
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }
            }

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
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    product.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
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

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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

data class FixedAsset(
    val id: Long,
    val name: String,
    val category: String,
    val purchasePrice: Double,
    val currentValue: Double,
    val depreciation: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedAssetsListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    val assets = remember {
        listOf(
            FixedAsset(1, "Office Laptop", "Electronics", 65000.0, 48750.0, 16250.0),
            FixedAsset(2, "Air Conditioner", "Appliances", 45000.0, 36000.0, 9000.0),
            FixedAsset(3, "Printer", "Electronics", 22000.0, 15400.0, 6600.0),
            FixedAsset(4, "Office Desk", "Furniture", 18000.0, 14400.0, 3600.0),
            FixedAsset(5, "Delivery Van", "Vehicle", 850000.0, 680000.0, 170000.0)
        )
    }

    val filteredAssets = assets.filter {
        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    val totalValue = assets.sumOf { it.purchasePrice }
    val totalCurrentValue = assets.sumOf { it.currentValue }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fixed Assets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddFixedAsset.route) },
                containerColor = VyaparFABBackground,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Asset", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search assets...", color = VyaparSearchHint) },
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
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Asset Value", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "\u20B9${String.format("%,.2f", totalValue)}",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueHeader
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Current Value", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    "\u20B9${String.format("%,.2f", totalCurrentValue)}",
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenBalance
                                )
                            }
                            Column {
                                Text("Total Depreciation", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    "\u20B9${String.format("%,.2f", totalValue - totalCurrentValue)}",
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RedAccent
                                )
                            }
                        }
                    }
                }
            }

            items(filteredAssets) { asset ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.FixedAssetDetail.createRoute(asset.id))
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(asset.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Surface(
                                color = LightBlueBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    asset.category,
                                    fontSize = 11.sp,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Purchase Price", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    "\u20B9${String.format("%,.0f", asset.purchasePrice)}",
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                                )
                            }
                            Column {
                                Text("Current Value", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    "\u20B9${String.format("%,.0f", asset.currentValue)}",
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenBalance
                                )
                            }
                            Column {
                                Text("Depreciation", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    "\u20B9${String.format("%,.0f", asset.depreciation)}",
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RedAccent
                                )
                            }
                        }
                    }
                }
            }

            if (filteredAssets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = VyaparEmptyStateIcon,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No assets found", fontSize = 16.sp, color = VyaparEmptyStateText)
                        }
                    }
                }
            }
        }
    }
}

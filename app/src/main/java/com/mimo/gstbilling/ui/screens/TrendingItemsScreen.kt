package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mimo.gstbilling.ui.theme.*

data class TrendingItem(val name: String, val sales: Int, val revenue: Double, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingItemsScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var sortBy by remember { mutableStateOf("Sales") }
    val categories = listOf("All", "Electronics", "Furniture", "Building Material", "Lighting", "Services")
    val sortOptions = listOf("Sales", "Revenue", "Recency")

    val allItems = listOf(
        TrendingItem("Laptop HP 15s", 45, 2100000.0, "Electronics"),
        TrendingItem("Office Chair", 32, 272000.0, "Furniture"),
        TrendingItem("LED Bulb 9W", 200, 24000.0, "Lighting"),
        TrendingItem("Printer Canon G3010", 18, 243000.0, "Electronics"),
        TrendingItem("Cement ACC 50kg", 150, 57000.0, "Building Material"),
        TrendingItem("Consulting Service", 25, 125000.0, "Services"),
        TrendingItem("Mouse Logitech", 80, 36000.0, "Electronics"),
        TrendingItem("Monitor LG 24 inch", 12, 168000.0, "Electronics")
    )

    val filteredItems = allItems.filter { selectedTab == 0 || it.category == categories[selectedTab] }
        .let { items ->
            when (sortBy) {
                "Revenue" -> items.sortedByDescending { it.revenue }
                "Recency" -> items.reversed()
                else -> items.sortedByDescending { it.sales }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trending Items", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = Primary, edgePadding = 8.dp) {
                    categories.forEachIndexed { index, category ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = {
                            Text(category, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Primary else TextSecondary, fontSize = 13.sp)
                        })
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sortOptions.forEach { option ->
                    FilterChip(
                        selected = sortBy == option,
                        onClick = { sortBy = option },
                        label = { Text(option, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary, selectedLabelColor = Color.White,
                            containerColor = Color.White, labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(borderColor = Divider, selectedBorderColor = Primary)
                    )
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${filteredItems.size} Trending Items", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    filteredItems.forEachIndexed { index, item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Text("#${index + 1}", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                Text("${item.sales} sales | ${item.category}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text("₹${String.format("%.0f", item.revenue)}", fontWeight = FontWeight.Bold, color = GreenBalance, fontSize = 14.sp)
                        }
                        if (index < filteredItems.lastIndex) HorizontalDivider(color = Divider)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

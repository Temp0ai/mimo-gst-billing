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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(navController: NavController, viewModel: ItemViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Products", "Services")
    val filteredItems = items.filter {
        it.name.contains(searchText, ignoreCase = true) && (selectedTab == 0 || (selectedTab == 1 && !it.isService) || (selectedTab == 2 && it.isService))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Items", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { navController.navigate(Screen.AddItem.route) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)) {
                    Icon(Icons.Filled.Add, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Add Item", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(modifier = Modifier.weight(1f).clickable { selectedTab = index }.background(if (isSelected) Color(0xFFFFEBEE) else Color.Transparent).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) RedAccent else TextSecondary)
                        }
                    }
                }
            }
            item {
                OutlinedTextField(value = searchText, onValueChange = { searchText = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("Search Items", fontSize = 14.sp) }, shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) })
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(filteredItems) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { navController.navigate(Screen.ItemDetail.createRoute(item.id)) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(item.name, fontWeight = FontWeight.Bold, color = TextPrimary); Text("HSN: ${item.hsnCode ?: "N/A"} | ${item.gstRate.toInt()}% GST", fontSize = 12.sp, color = TextSecondary) }
                        Column(horizontalAlignment = Alignment.End) { Text(String.format(java.util.Locale.US, "\u20B9%,.2f", item.salePrice), fontWeight = FontWeight.Bold, color = Primary); Text("Stock: ${item.stockQuantity.toInt()}", fontSize = 12.sp, color = TextSecondary) }
                    }
                }
            }
            item { if (filteredItems.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No items found", fontSize = 14.sp, color = TextSecondary) } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

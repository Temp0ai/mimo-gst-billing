package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemPriceListScreen(navController: NavController, viewModel: ItemViewModel = hiltViewModel()) {
    val items by viewModel.allItems.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Price List", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) { Text("Item", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary); Text("Sale Price", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary); Text("Purchase", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary) } } }
            items(items) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(item.name, modifier = Modifier.weight(1f), fontSize = 13.sp, color = TextPrimary); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", item.salePrice), modifier = Modifier.weight(0.5f), fontSize = 13.sp, color = GreenBalance); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", item.purchasePrice), modifier = Modifier.weight(0.5f), fontSize = 13.sp, color = RedAccent) } }
            }
            item { if (items.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("No items found", color = TextSecondary) } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController, viewModel: OrderViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Estimate", "Quotation", "Order")
    val orders by viewModel.getOrders(tabs[selectedTab].lowercase()).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    if (showAddDialog) {
        var partyName by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create ${tabs[selectedTab]}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = partyName, onValueChange = { partyName = it }, label = { Text("Party Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (partyName.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0) {
                        viewModel.addOrder(1L, "${tabs[selectedTab].uppercase()}-${System.currentTimeMillis() % 10000}", tabs[selectedTab].lowercase(), amount.toDoubleOrNull() ?: 0.0, 0.0, 0.0, notes.ifBlank { null })
                        showAddDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = GreenBalance, contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = "Create")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5))) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(if (isSelected) Color(0xFFFFEBEE) else Color.Transparent).clickable { selectedTab = index }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) RedAccent else TextSecondary)
                    }
                }
            }
            if (orders.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("No ${tabs[selectedTab].lowercase()}s yet", fontSize = 16.sp, color = TextSecondary)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(orders) { order ->
                        Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.orderNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(dateFormat.format(Date(order.orderDate)), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(String.format(Locale.US, "\u20B9%,.2f", order.totalAmount), fontWeight = FontWeight.Bold, color = Primary)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if (order.status == "completed") Icons.Filled.CheckCircle else Icons.Filled.Pending, contentDescription = null, tint = if (order.status == "completed") GreenBalance else RedAccent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(order.status.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = if (order.status == "completed") GreenBalance else RedAccent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

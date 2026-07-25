package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController, viewModel: OrderViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Sales Orders", "Purchase Orders")
    val orderTypes = listOf("sales_order", "purchase_order")
    val orders by viewModel.getOrders(orderTypes[selectedTab]).collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<Long?>(null) }

    if (showDeleteDialog && orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; orderToDelete = null },
            title = { Text("Delete Order", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this order?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteOrder(orderToDelete!!)
                    showDeleteDialog = false
                    orderToDelete = null
                }) { Text("Delete", color = RedAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; orderToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateOrder.createRoute(orderType = orderTypes[selectedTab])) },
                containerColor = RedAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Order")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(if (isSelected) Color(0xFFFFEBEE) else Color.White)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) RedAccent else TextSecondary
                        )
                    }
                }
            }

            if (orders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No orders yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap + to create an order", fontSize = 13.sp, color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orders) { order ->
                        var partyName by remember { mutableStateOf("Loading...") }
                        LaunchedEffect(order.partyId) {
                            partyName = viewModel.getPartyName(order.partyId)
                        }
                        val statusColor = when (order.status) {
                            "pending" -> Warning
                            "confirmed" -> Primary
                            "completed" -> GreenBalance
                            "cancelled" -> RedAccent
                            else -> TextSecondary
                        }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.clickable {
                                navController.navigate(com.mimo.gstbilling.ui.navigation.Screen.OrderDetail.createRoute(order.id))
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.orderNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(partyName, fontSize = 12.sp, color = TextSecondary)
                                    Text(dateFormat.format(Date(order.orderDate)), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        String.format(Locale.US, "\u20B9%,.2f", order.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            order.status.replaceFirstChar { it.uppercase() },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
}

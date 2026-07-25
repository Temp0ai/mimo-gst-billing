package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.OrderEntity
import com.mimo.gstbilling.data.local.entity.OrderItemEntity
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    orderId: Long,
    viewModel: OrderViewModel = hiltViewModel()
) {
    var order by remember { mutableStateOf<OrderEntity?>(null) }
    var orderItems by remember { mutableStateOf<List<OrderItemEntity>>(emptyList()) }
    var partyName by remember { mutableStateOf("Loading...") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConvertDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.getOrderById(orderId).collect { order = it }
    }

    LaunchedEffect(order) {
        order?.let {
            partyName = viewModel.getPartyName(it.partyId)
            viewModel.getOrderItems(it.id).collect { orderItems = it }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Order", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this order?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteOrder(orderId)
                    showDeleteDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = RedAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showConvertDialog) {
        AlertDialog(
            onDismissRequest = { showConvertDialog = false },
            title = { Text("Convert to Invoice", fontWeight = FontWeight.Bold) },
            text = { Text("This will create an invoice from this order. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showConvertDialog = false
                    scope.launch {
                        viewModel.convertOrderToInvoice(orderId).collect { invoiceId ->
                            navController.popBackStack()
                            navController.navigate(com.mimo.gstbilling.ui.navigation.Screen.InvoiceDetail.createRoute(invoiceId))
                        }
                    }
                }) { Text("Convert") }
            },
            dismissButton = {
                TextButton(onClick = { showConvertDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(order?.orderNumber ?: "Order Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        order?.let { o ->
                            val message = buildString {
                                append("Order #${o.orderNumber}\n")
                                append("Date: ${dateFormat.format(Date(o.orderDate))}\n")
                                append("Party: $partyName\n\n")
                                orderItems.forEach { item ->
                                    append("• ${item.itemName}: ₹${String.format(Locale.US, "%,.2f", item.totalAmount)}\n")
                                }
                                append("\nTotal: ₹${String.format(Locale.US, "%,.2f", o.totalAmount)}")
                            }
                            val encoded = Uri.encode(message)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$encoded"))
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                    }
                    IconButton(onClick = {
                        order?.let { o ->
                            val message = buildString {
                                append("Order #${o.orderNumber}\n")
                                append("Date: ${dateFormat.format(Date(o.orderDate))}\n")
                                append("Party: $partyName\n\n")
                                orderItems.forEach { item ->
                                    append("• ${item.itemName}: ₹${String.format(Locale.US, "%,.2f", item.totalAmount)}\n")
                                }
                                append("\nTotal: ₹${String.format(Locale.US, "%,.2f", o.totalAmount)}")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            context.startActivity(Intent.createChooser(intent, "Print Order"))
                        }
                    }) {
                        Icon(Icons.Filled.Print, contentDescription = "Print / Share", tint = Primary)
                    }
                    IconButton(onClick = {
                        order?.let { o ->
                            navController.navigate(Screen.CreateOrder.createRoute(orderType = o.orderType))
                        }
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Order")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        order?.let { o ->
            val statusColor = when (o.status) {
                "pending" -> Warning
                "confirmed" -> Primary
                "completed" -> GreenBalance
                "cancelled" -> RedAccent
                else -> TextSecondary
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Info
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(o.orderNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.12f)) {
                                    Text(o.status.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = statusColor, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Party: $partyName", color = TextSecondary)
                            Text("Date: ${dateFormat.format(Date(o.orderDate))}", color = TextSecondary)
                            if (o.dueDate != null) {
                                Text("Due Date: ${dateFormat.format(Date(o.dueDate!!))}", color = TextSecondary)
                            }
                            Text("Type: ${o.orderType.replace("_", " ").replaceFirstChar { it.uppercase() }}", color = TextSecondary)
                        }
                    }
                }

                // Items
                item {
                    Text("Items", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
                }

                itemsIndexed(orderItems) { index, item ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.itemName, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Qty: ${item.quantity} ${item.unit} | Rate: ${String.format(Locale.US, "\u20B9%.2f", item.price)}", fontSize = 12.sp, color = TextSecondary)
                                Text("GST: ${item.gstRate}% | Taxable: ${String.format(Locale.US, "\u20B9%.2f", item.taxableAmount)}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(String.format(Locale.US, "\u20B9%.2f", item.totalAmount), fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }

                // Summary
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Summary", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sub Total", color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", o.subTotal))
                            }
                            if (o.discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount", color = TextSecondary)
                                    Text(String.format(Locale.US, "\u20B9%,.2f", o.discount))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CGST", color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", o.cgstTotal))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SGST", color = TextSecondary)
                                Text(String.format(Locale.US, "\u20B9%,.2f", o.sgstTotal))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(String.format(Locale.US, "\u20B9%,.2f", o.totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary)
                            }
                        }
                    }
                }

                // Status Update Buttons
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (o.status == "pending") {
                            Button(
                                onClick = { viewModel.updateOrderStatus(orderId, "confirmed") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) { Text("Confirm") }
                        }
                        if (o.status == "confirmed") {
                            Button(
                                onClick = { viewModel.updateOrderStatus(orderId, "completed") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)
                            ) { Text("Complete") }
                        }
                        if (o.status != "cancelled" && o.status != "completed") {
                            OutlinedButton(
                                onClick = { viewModel.updateOrderStatus(orderId, "cancelled") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50)
                            ) { Text("Cancel", color = RedAccent) }
                        }
                    }
                }

                // Convert to Invoice
                if (o.status == "confirmed") {
                    item {
                        Button(
                            onClick = { showConvertDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                        ) {
                            Text("Convert to Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        } ?: Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    }
}

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
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
import com.mimo.gstbilling.data.local.entity.ItemEntity
import com.mimo.gstbilling.data.local.entity.OrderItemEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.OrderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class OrderItemModel(
    val itemId: Long = 0,
    val itemName: String = "",
    val hsnCode: String = "",
    val quantity: Double = 1.0,
    val unit: String = "Pcs",
    val price: Double = 0.0,
    val gstRate: Double = 18.0,
    val taxableAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalAmount: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    navController: NavController,
    orderType: String = "sales_order",
    viewModel: OrderViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val isSales = orderType == "sales_order"
    val title = if (isSales) "Create Sale Order" else "Create Purchase Order"

    var selectedPartyId by remember { mutableLongStateOf(0L) }
    var selectedPartyName by remember { mutableStateOf("") }
    var orderDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var dueDate by remember { mutableLongStateOf(0L) }
    var hasDueDate by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf(listOf<OrderItemModel>()) }
    var notes by remember { mutableStateOf("") }
    var showPartyDropdown by remember { mutableStateOf(false) }
    var showItemPicker by remember { mutableStateOf(false) }
    var partySearchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    val parties = remember { mutableStateListOf<PartyEntity>() }
    val allItems = remember { mutableStateListOf<ItemEntity>() }

    val filteredParties = parties.filter {
        it.name.contains(partySearchQuery, ignoreCase = true) ||
        it.phone?.contains(partySearchQuery, ignoreCase = true) == true
    }

    val subTotal = items.sumOf { it.taxableAmount }
    val totalCgst = items.sumOf { it.cgstAmount }
    val totalSgst = items.sumOf { it.sgstAmount }
    val totalIgst = items.sumOf { it.igstAmount }
    val totalAmount = items.sumOf { it.totalAmount }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = orderDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { orderDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = if (dueDate > 0) dueDate else System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it; hasDueDate = true }
                    showDueDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showItemPicker) {
        AlertDialog(
            onDismissRequest = { showItemPicker = false },
            title = { Text("Select Item", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn {
                    items(allItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val qty = 1.0
                                    val taxable = item.salePrice * qty
                                    val cgst = taxable * item.gstRate / 200
                                    val sgst = taxable * item.gstRate / 200
                                    val total = taxable + cgst + sgst
                                    items = items + OrderItemModel(
                                        itemId = item.id,
                                        itemName = item.name,
                                        hsnCode = item.hsnCode ?: "",
                                        quantity = qty,
                                        unit = item.unit,
                                        price = item.salePrice,
                                        gstRate = item.gstRate,
                                        taxableAmount = taxable,
                                        cgstAmount = cgst,
                                        sgstAmount = sgst,
                                        totalAmount = total
                                    )
                                    showItemPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Medium)
                                Text("HSN: ${item.hsnCode ?: "N/A"} | GST: ${item.gstRate}%", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(String.format(Locale.US, "\u20B9%.2f", item.salePrice), fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showItemPicker = false }) { Text("Close") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Party", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = if (selectedPartyId > 0) selectedPartyName else partySearchQuery,
                            onValueChange = {
                                partySearchQuery = it
                                selectedPartyId = 0
                                selectedPartyName = ""
                                showPartyDropdown = true
                            },
                            label = { Text("Search party...") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showPartyDropdown = !showPartyDropdown })
                            }
                        )
                        if (showPartyDropdown && partySearchQuery.isNotBlank()) {
                            filteredParties.forEach { party ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedPartyId = party.id
                                        selectedPartyName = party.name
                                        showPartyDropdown = false
                                        partySearchQuery = ""
                                    }.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(party.name, fontWeight = FontWeight.Medium)
                                        Text(party.phone ?: "", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Order Date", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(dateFormat.format(Date(orderDate)))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Due Date (Optional)", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(onClick = { showDueDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (hasDueDate && dueDate > 0) dateFormat.format(Date(dueDate)) else "Select")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Items", fontWeight = FontWeight.Bold, color = TextPrimary)
                            TextButton(onClick = { showItemPicker = true }) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Items")
                            }
                        }
                        if (items.isEmpty()) {
                            Text("No items added", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }
            }

            items(items.size) { index ->
                val item = items[index]
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.itemName, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("HSN: ${item.hsnCode} | Qty: ${item.quantity} | Rate: ${String.format(Locale.US, "\u20B9%.2f", item.price)}", fontSize = 12.sp, color = TextSecondary)
                            Text("GST: ${item.gstRate}% | Total: ${String.format(Locale.US, "\u20B9%.2f", item.totalAmount)}", fontSize = 12.sp, color = Primary)
                        }
                        IconButton(onClick = { items = items.toMutableList().apply { removeAt(index) } }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = RedAccent)
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notes", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Add notes...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sub Total", color = TextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", subTotal))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST", color = TextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalCgst))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST", color = TextSecondary)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalSgst))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(String.format(Locale.US, "\u20B9%,.2f", totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (selectedPartyId > 0 && items.isNotEmpty() && !isSaving) {
                            isSaving = true
                            scope.launch {
                                val orderNum = viewModel.getOrderNumber(orderType)
                                val order = com.mimo.gstbilling.data.local.entity.OrderEntity(
                                    companyId = 1L,
                                    partyId = selectedPartyId,
                                    orderNumber = orderNum,
                                    orderDate = orderDate,
                                    dueDate = if (hasDueDate && dueDate > 0) dueDate else null,
                                    orderType = orderType,
                                    subTotal = subTotal,
                                    taxableAmount = items.sumOf { it.taxableAmount },
                                    cgstTotal = totalCgst,
                                    sgstTotal = totalSgst,
                                    igstTotal = totalIgst,
                                    totalAmount = totalAmount,
                                    notes = notes.ifBlank { null }
                                )
                                val orderItems = items.map { m ->
                                    OrderItemEntity(
                                        orderId = 0,
                                        itemId = m.itemId,
                                        itemName = m.itemName,
                                        hsnCode = m.hsnCode.ifBlank { null },
                                        quantity = m.quantity,
                                        unit = m.unit,
                                        price = m.price,
                                        gstRate = m.gstRate,
                                        taxableAmount = m.taxableAmount,
                                        cgstAmount = m.cgstAmount,
                                        sgstAmount = m.sgstAmount,
                                        igstAmount = 0.0,
                                        totalAmount = m.totalAmount
                                    )
                                }
                                viewModel.createOrder(order, orderItems).collect {
                                    navController.popBackStack()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    enabled = selectedPartyId > 0 && items.isNotEmpty() && !isSaving
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

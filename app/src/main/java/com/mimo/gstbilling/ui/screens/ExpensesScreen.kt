package com.mimo.gstbilling.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

val expenseCategories = listOf("Rent", "Salary", "Utilities", "Transport", "Office Supplies", "Marketing", "Phone/Internet", "Travel", "Other")
val paymentModes = listOf("Cash", "UPI", "Bank Transfer", "Credit Card", "Debit Card")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(navController: NavController, viewModel: ExpenseViewModel = hiltViewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showDeleteConfirm by remember { mutableStateOf<ExpenseEntity?>(null) }

    val filteredExpenses = if (selectedCategory == "All") expenses else expenses.filter { it.category == selectedCategory }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Expense", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this expense of ₹${String.format(Locale.US, "%,.2f", showDeleteConfirm!!.amount)}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteExpense(showDeleteConfirm!!); showDeleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } }
        )
    }

    if (showAddEditDialog) {
        AddEditExpenseDialog(
            expense = editingExpense,
            onDismiss = { showAddEditDialog = false; editingExpense = null },
            onSave = { category, amount, date, description, paymentMode ->
                if (editingExpense != null) {
                    viewModel.editExpense(editingExpense!!.copy(category = category, amount = amount, date = date, description = description, paymentMode = paymentMode))
                } else {
                    viewModel.addExpense(category, amount, date, description, paymentMode)
                }
                showAddEditDialog = false; editingExpense = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingExpense = null; showAddEditDialog = true }, containerColor = GreenBalance, contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Receipt, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Total Expenses", fontSize = 14.sp, color = RedAccent, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.US, "\u20B9%,.2f", totalExpenses), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
            item {
                androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = selectedCategory == "All", onClick = { selectedCategory = "All" }, label = { Text("All") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
                    }
                    items(expenseCategories) { cat ->
                        FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary.copy(alpha = 0.12f), selectedLabelColor = Primary))
                    }
                }
            }
            items(filteredExpenses, key = { it.id }) { expense ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { editingExpense = expense; showAddEditDialog = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = RedAccent.copy(alpha = 0.1f))) {
                                Icon(Icons.Filled.Receipt, contentDescription = null, tint = RedAccent, modifier = Modifier.padding(8.dp).size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.category, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(expense.description ?: expense.paymentMode, fontSize = 12.sp, color = TextSecondary)
                            Text(dateFormat.format(Date(expense.date)), fontSize = 11.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format(Locale.US, "-\u20B9%,.2f", expense.amount), fontWeight = FontWeight.Bold, color = RedAccent, fontSize = 14.sp)
                            IconButton(onClick = { showDeleteConfirm = expense }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            item { if (filteredExpenses.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Receipt, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("No expenses yet", fontSize = 14.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(expense: ExpenseEntity?, onDismiss: () -> Unit, onSave: (String, Double, Long, String?, String) -> Unit) {
    val context = LocalContext.current
    var category by remember { mutableStateOf(expense?.category ?: expenseCategories[0]) }
    var amount by remember { mutableStateOf(expense?.amount?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var description by remember { mutableStateOf(expense?.description ?: "") }
    var paymentMode by remember { mutableStateOf(expense?.paymentMode ?: paymentModes[0]) }
    var dateMillis by remember { mutableStateOf(expense?.date ?: System.currentTimeMillis()) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentDropdown by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }
    val isEditing = expense != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Expense" else "Add Expense", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category *") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showCategoryDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                    DropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                        expenseCategories.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { category = c; showCategoryDropdown = false }) }
                    }
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dateFormat.format(Date(dateMillis)), onValueChange = {}, readOnly = true, label = { Text("Date") }, trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Date", modifier = Modifier.clickable { val c = Calendar.getInstance().apply { timeInMillis = dateMillis }; DatePickerDialog(context, { _, y, m, d -> val cal = Calendar.getInstance(); cal.set(y, m, d); dateMillis = cal.timeInMillis }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }) }, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedTextField(value = paymentMode, onValueChange = {}, readOnly = true, label = { Text("Payment Mode") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showPaymentDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                    DropdownMenu(expanded = showPaymentDropdown, onDismissRequest = { showPaymentDropdown = false }) {
                        paymentModes.forEach { m -> DropdownMenuItem(text = { Text(m) }, onClick = { paymentMode = m; showPaymentDropdown = false }) }
                    }
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (category.isNotBlank() && amt > 0) onSave(category, amt, dateMillis, description.ifBlank { null }, paymentMode)
            }, enabled = amount.toDoubleOrNull() != null && amount.toDouble() > 0) { Text(if (isEditing) "Update" else "Add", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

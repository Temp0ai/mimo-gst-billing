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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.RecurringInvoiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringInvoicesScreen(navController: NavController, viewModel: RecurringInvoiceViewModel = hiltViewModel()) {
    val recurring by viewModel.recurring.collectAsState()
    val parties by viewModel.parties.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val frequencies = listOf("weekly", "monthly", "quarterly", "yearly")

    if (showAddDialog) {
        var selectedPartyId by remember { mutableLongStateOf(0L) }
        var selectedPartyName by remember { mutableStateOf("") }
        var frequency by remember { mutableStateOf("monthly") }
        var amount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var showPartyDropdown by remember { mutableStateOf(false) }
        var showFreqDropdown by remember { mutableStateOf(false) }

        AlertDialog(onDismissRequest = { showAddDialog = false },
            title = { Text("Create Recurring Invoice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box {
                        OutlinedTextField(value = selectedPartyName, onValueChange = {}, readOnly = true, label = { Text("Party *") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showPartyDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showPartyDropdown, onDismissRequest = { showPartyDropdown = false }) {
                            parties.forEach { p -> DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedPartyId = p.id; selectedPartyName = p.name; showPartyDropdown = false }) }
                        }
                    }
                    Box {
                        OutlinedTextField(value = frequency.replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true, label = { Text("Frequency") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showFreqDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                        DropdownMenu(expanded = showFreqDropdown, onDismissRequest = { showFreqDropdown = false }) {
                            frequencies.forEach { f -> DropdownMenuItem(text = { Text(f.replaceFirstChar { it.uppercase() }) }, onClick = { frequency = f; showFreqDropdown = false }) }
                        }
                    }
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                }
            },
            confirmButton = { TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (selectedPartyId > 0 && amt > 0) {
                    val cal = Calendar.getInstance()
                    when (frequency) { "weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1); "monthly" -> cal.add(Calendar.MONTH, 1); "quarterly" -> cal.add(Calendar.MONTH, 3); "yearly" -> cal.add(Calendar.YEAR, 1) }
                    viewModel.addRecurring(selectedPartyId, selectedPartyName, frequency, amt, description.ifBlank { null }, "sales", cal.timeInMillis)
                    showAddDialog = false
                }
            }, enabled = selectedPartyId > 0 && (amount.toDoubleOrNull() ?: 0.0) > 0) { Text("Create", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recurring Invoices", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Primary, contentColor = Color.White) { Icon(Icons.Filled.Add, contentDescription = "Add Recurring") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            items(recurring, key = { it.id }) { rec ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = if (rec.isActive) GreenBalance.copy(alpha = 0.1f) else TextSecondary.copy(alpha = 0.1f))) {
                                Icon(Icons.Filled.Repeat, contentDescription = null, tint = if (rec.isActive) GreenBalance else TextSecondary, modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rec.partyName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("${rec.frequency.replaceFirstChar { it.uppercase() }} \u2022 ${String.format(Locale.US, "\u20B9%,.2f", rec.amount)}", fontSize = 12.sp, color = Primary)
                            Text("Next: ${dateFormat.format(Date(rec.nextDueDate))}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = rec.isActive, onCheckedChange = { viewModel.toggleActive(rec) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenBalance))
                    }
                }
            }
            item { if (recurring.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Repeat, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("No recurring invoices", fontSize = 14.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

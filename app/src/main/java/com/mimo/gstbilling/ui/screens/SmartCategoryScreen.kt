package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.utils.SmartCategorizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RecentExpense(
    val description: String,
    val amount: Double,
    val category: String
)

@HiltViewModel
class SmartCategoryViewModel @Inject constructor() : ViewModel() {

    private val _recentExpenses = MutableStateFlow(
        listOf(
            RecentExpense("Office Rent - June", 25000.0, "Rent"),
            RecentExpense("Staff Salary", 45000.0, "Salary"),
            RecentExpense("Tea & Snacks for meeting", 350.0, "Food"),
            RecentExpense("Uber to client office", 420.0, "Travel"),
            RecentExpense("Printer Paper A4", 650.0, "Office Supplies"),
            RecentExpense("Electricity Bill", 2800.0, "Utilities"),
            RecentExpense("CA Audit Fees", 5000.0, "Professional Services"),
            RecentExpense("AC Repair", 1200.0, "Maintenance"),
            RecentExpense("Life Insurance Premium", 15000.0, "Insurance"),
            RecentExpense("GST Payment", 12500.0, "Tax")
        )
    )
    val recentExpenses = _recentExpenses.asStateFlow()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCategoryScreen(
    navController: NavController,
    viewModel: SmartCategoryViewModel = hiltViewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val recentExpenses by viewModel.recentExpenses.collectAsState()

    val category = remember(inputText) {
        if (inputText.isNotBlank()) SmartCategorizer.categorizeExpense(inputText) else ""
    }
    val hsnResult = remember(inputText) {
        if (inputText.isNotBlank()) SmartCategorizer.suggestHsnCode(inputText) else null
    }
    val taxRate = remember(hsnResult) {
        hsnResult?.let { SmartCategorizer.getTaxRate(it.first) } ?: 18.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Auto-Categorize", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Type an expense description or item name",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Office Rent, Tea, Laptop...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (inputText.isNotBlank()) {
                                    IconButton(onClick = { inputText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Divider,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            if (inputText.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Results", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Category: ", fontSize = 14.sp, color = TextSecondary)
                                val chipColor = Color(SmartCategorizer.getCategoryColor(category))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(chipColor.copy(alpha = 0.15f))
                                        .border(1.dp, chipColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipColor)
                                }
                            }

                            if (hsnResult != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("HSN Code: ", fontSize = 14.sp, color = TextSecondary)
                                    Text("${hsnResult.first} - ${hsnResult.second}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tax Rate: ", fontSize = 14.sp, color = TextSecondary)
                                    Text("${taxRate.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenBalance)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Quick Test", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTestChip("Office Rent", modifier = Modifier.weight(1f)) { inputText = it }
                    QuickTestChip("Tea and Snacks", modifier = Modifier.weight(1f)) { inputText = it }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTestChip("Salary Payment", modifier = Modifier.weight(1f)) { inputText = it }
                    QuickTestChip("Travel - Uber", modifier = Modifier.weight(1f)) { inputText = it }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Recent Expenses", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }

            items(recentExpenses) { expense ->
                val catColor = Color(SmartCategorizer.getCategoryColor(expense.category))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { inputText = expense.description }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                expense.description,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(catColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(expense.category, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = catColor)
                            }
                        }
                        Text(
                            "Rs. ${String.format("%.0f", expense.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QuickTestChip(text: String, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Divider, RoundedCornerShape(10.dp))
            .clickable { onClick(text) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

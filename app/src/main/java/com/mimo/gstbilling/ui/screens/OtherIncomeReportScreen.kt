package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class OtherIncome(
    val id: Long, val name: String, val amount: Double, val date: Long, val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherIncomeReportScreen(navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val incomes = remember { listOf(
        OtherIncome(1, "Commission Received", 5000.0, System.currentTimeMillis() - 86400000 * 3, "Commission"),
        OtherIncome(2, "Rent Income", 15000.0, System.currentTimeMillis() - 86400000 * 10, "Rent"),
        OtherIncome(3, "Interest Earned", 2500.0, System.currentTimeMillis() - 86400000 * 15, "Interest"),
        OtherIncome(4, "Sale of Scrap", 3200.0, System.currentTimeMillis() - 86400000 * 7, "Scrap"),
        OtherIncome(5, "Discount Received", 1800.0, System.currentTimeMillis() - 86400000 * 20, "Discount")
    ) }
    val totalIncome = incomes.sumOf { it.amount }
    val grouped = incomes.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Other Income Report", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Other Income", fontSize = 14.sp, color = VyaparTextSecondary)
                        Text("₹${String.format("%,.2f", totalIncome)}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        Text("${incomes.size} entries", fontSize = 12.sp, color = VyaparTextSecondary)
                    }
                }
            }
            grouped.forEach { (category, items) ->
                item {
                    Text(category.uppercase(), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VyaparTextSecondary)
                }
                items(items) { income ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(VyaparGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) { Text(income.name, fontWeight = FontWeight.Medium, fontSize = 14.sp); Text(dateFormat.format(Date(income.date)), fontSize = 12.sp, color = VyaparTextSecondary) }
                            Text("₹${String.format("%,.2f", income.amount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VyaparGreen)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

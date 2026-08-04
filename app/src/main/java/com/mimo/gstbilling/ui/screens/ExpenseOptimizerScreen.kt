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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ExpenseOptimizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseOptimizerScreen(navController: NavController, viewModel: ExpenseOptimizerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Expense Optimizer", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        if (uiState.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.suggestions.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Savings, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("Expenses look optimized", fontSize = 16.sp, color = TextSecondary) } } }
        else { LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            item { Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Savings Potential", fontSize = 14.sp, color = TextSecondary); Text("₹${String.format("%,.0f", uiState.suggestions.sumOf { it.savingPotential })}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VyaparGreen); Text("${uiState.suggestions.size} categories to review", fontSize = 12.sp, color = TextSecondary) } } }
            items(uiState.suggestions) { s -> val priColor = when(s.priority) { "HIGH" -> VyaparRed; "MEDIUM" -> Color(0xFFFF8A00); else -> VyaparGreen }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(s.category, fontWeight = FontWeight.Bold, fontSize = 15.sp); Surface(shape = RoundedCornerShape(50), color = priColor.copy(alpha = 0.1f)) { Text(s.priority, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = priColor) } }; Spacer(Modifier.height(4.dp)); Text(s.suggestion, fontSize = 12.sp, color = TextSecondary); Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Current: ₹${String.format("%,.0f", s.currentMonthly)}/mo", fontSize = 12.sp, color = TextSecondary); Text("Avg: ₹${String.format("%,.0f", s.avgMonthly)}/mo", fontSize = 12.sp, color = TextSecondary) }; Text("Save ₹${String.format("%,.0f", s.savingPotential)}/month", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparGreen) } } }
            item { Spacer(Modifier.height(16.dp)) }
        } }
    }
}

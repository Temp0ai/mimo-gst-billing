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
import com.mimo.gstbilling.ui.viewmodel.SalesTrendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesTrendScreen(navController: NavController, viewModel: SalesTrendViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Sales Trend Predictor", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        if (uiState.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.forecasts.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.ShowChart, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("Not enough data for trends", fontSize = 16.sp, color = TextSecondary) } } }
        else { LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            item { Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("3-Month Forecast", fontSize = 14.sp, color = TextSecondary); Text("₹${String.format("%,.0f", uiState.forecasts.sumOf { it.predictedAmount })}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VyaparBlue); Text("predicted total", fontSize = 12.sp, color = TextSecondary) } } }
            items(uiState.forecasts) { f -> val trendColor = when(f.trend) { "Upward" -> VyaparGreen; "Downward" -> VyaparRed; else -> Color(0xFFFFC107) }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(f.month, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text(f.trend, fontSize = 12.sp, color = trendColor) }; Column(horizontalAlignment = Alignment.End) { Text("₹${String.format("%,.0f", f.predictedAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparBlue); Text("${f.confidence}% confidence", fontSize = 11.sp, color = TextSecondary) } } } }
            item { Spacer(Modifier.height(16.dp)) }
        } }
    }
}

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
import com.mimo.gstbilling.ui.viewmodel.CustomerChurnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerChurnScreen(navController: NavController, viewModel: CustomerChurnViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Customer Churn Predictor", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        if (uiState.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (uiState.risks.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.People, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp)); Spacer(Modifier.height(12.dp)); Text("No churn risk data", fontSize = 16.sp, color = TextSecondary) } } }
        else { LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            item { Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("At-Risk Customers", fontSize = 14.sp, color = TextSecondary); Text("${uiState.risks.count { it.riskLevel == "HIGH" }}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VyaparRed); Text("high risk customers", fontSize = 12.sp, color = TextSecondary) } } }
            items(uiState.risks) { r -> val riskColor = when(r.riskLevel) { "HIGH" -> VyaparRed; "MEDIUM" -> Color(0xFFFF8A00); else -> VyaparGreen }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(r.partyName, fontWeight = FontWeight.Bold, fontSize = 15.sp); Surface(shape = RoundedCornerShape(50), color = riskColor.copy(alpha = 0.1f)) { Text(r.riskLevel, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = riskColor) } }; Spacer(Modifier.height(4.dp)); Text(r.reason, fontSize = 12.sp, color = TextSecondary); Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Last purchase: ${r.lastPurchaseDays} days ago", fontSize = 11.sp, color = TextSecondary); Text("Risk: ${r.riskScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = riskColor) } } } }
            item { Spacer(Modifier.height(16.dp)) }
        } }
    }
}

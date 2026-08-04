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
import com.mimo.gstbilling.ui.viewmodel.BusinessHealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHealthScreen(navController: NavController, viewModel: BusinessHealthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val h = uiState.health
    val healthColor = when { h.overall >= 80 -> VyaparGreen; h.overall >= 65 -> Color(0xFF4CAF50); h.overall >= 50 -> Color(0xFFFFC107); h.overall >= 35 -> Color(0xFFFF8A00); else -> VyaparRed }
    Scaffold(topBar = { TopAppBar(title = { Text("Business Health Score", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            item { Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Overall Score", fontSize = 14.sp, color = TextSecondary); Text("${h.overall}/100", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = healthColor); Text(h.rating, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = healthColor); Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Revenue", fontSize = 11.sp, color = TextSecondary); Text("${h.revenue}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparBlue) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Collections", fontSize = 11.sp, color = TextSecondary); Text("${h.collections}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparGreen) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Expenses", fontSize = 11.sp, color = TextSecondary); Text("${h.expenses}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A00)) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Growth", fontSize = 11.sp, color = TextSecondary); Text("${h.growth}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparRed) } } } } }
            if (h.factors.isNotEmpty()) { item { Text("Key Factors", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }; items(h.factors) { f -> Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Warning, null, tint = Color(0xFFFF8A00), modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(f, fontSize = 13.sp, color = TextSecondary) } } } }
            if (h.suggestions.isNotEmpty()) { item { Text("Suggestions", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }; items(h.suggestions) { s -> Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Lightbulb, null, tint = VyaparBlue, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(s, fontSize = 13.sp, color = TextPrimary) } } } }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

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
import com.mimo.gstbilling.ui.viewmodel.GstFilingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstFilingScreen(navController: NavController, viewModel: GstFilingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("GST Filing Reminders", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).background(VyaparBackground)) {
            items(uiState.deadlines) { d -> val statusColor = when(d.status) { "URGENT" -> VyaparRed; "UPCOMING" -> Color(0xFFFF8A00); else -> VyaparGreen }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(d.returnType, fontWeight = FontWeight.Bold, fontSize = 16.sp); Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.1f)) { Text(d.status, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor) } }; Spacer(Modifier.height(4.dp)); Text(d.description, fontSize = 12.sp, color = TextSecondary); Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Due: ${d.dueDate}", fontSize = 13.sp, fontWeight = FontWeight.Medium); Text("${d.daysRemaining} days left", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = statusColor) }; Text(d.period, fontSize = 11.sp, color = TextSecondary) } } }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

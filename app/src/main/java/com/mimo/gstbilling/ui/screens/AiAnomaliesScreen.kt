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
import com.mimo.gstbilling.ui.viewmodel.AiAnomaliesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnomaliesScreen(navController: NavController, viewModel: AiAnomaliesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anomaly Detection", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Risk Score", fontSize = 14.sp, color = VyaparTextSecondary)
                        Text(
                            "${String.format("%.0f", uiState.riskScore)}/100",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                uiState.riskScore > 66 -> VyaparRed
                                uiState.riskScore > 33 -> Color(0xFFFF9800)
                                else -> VyaparGreen
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("${uiState.highCount}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VyaparRed)
                                Text("High", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column {
                                Text("${uiState.mediumCount}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                                Text("Medium", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Column {
                                Text("${uiState.lowCount}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                                Text("Low", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.runDetection() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue),
                    enabled = !uiState.isScanning
                ) {
                    if (uiState.isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanning...")
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Detection", fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(uiState.anomalies) { anomaly ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when (anomaly.severity) {
                                    "high" -> VyaparRed.copy(alpha = 0.1f)
                                    "medium" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                    else -> VyaparGreen.copy(alpha = 0.1f)
                                }
                            ) {
                                Text(
                                    anomaly.severity.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (anomaly.severity) {
                                        "high" -> VyaparRed
                                        "medium" -> Color(0xFFFF9800)
                                        else -> VyaparGreen
                                    }
                                )
                            }
                            Text(anomaly.type.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(anomaly.description, fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }

            if (uiState.anomalies.isEmpty() && !uiState.isScanning) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No anomalies detected!", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Your data looks normal", fontSize = 14.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

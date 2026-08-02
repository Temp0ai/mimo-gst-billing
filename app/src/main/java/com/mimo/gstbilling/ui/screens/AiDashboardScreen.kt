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
import com.mimo.gstbilling.ui.viewmodel.AiDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDashboardScreen(navController: NavController, viewModel: AiDashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Dashboard", fontWeight = FontWeight.Bold) },
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
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("AI Business Assistant", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Intelligent insights for your business", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("${uiState.insightsCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Insights", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                            Column {
                                Text("${uiState.alertsCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Alerts", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                            Column {
                                Text("${uiState.suggestionsCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Suggestions", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Quick Actions",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            val quickActions = listOf(
                Triple("Smart Reminders", Icons.Filled.Notifications, "Generate payment reminders") to "reminders",
                Triple("Business Insights", Icons.Filled.Analytics, "View business analytics") to "insights",
                Triple("Cash Flow Forecast", Icons.Filled.TrendingUp, "Predict future cash flow") to "cashflow",
                Triple("Tax Advisor", Icons.Filled.Calculate, "Get tax saving tips") to "tax",
                Triple("Duplicate Check", Icons.Filled.ContentCopy, "Find duplicate entries") to "duplicates",
                Triple("Anomaly Detection", Icons.Filled.Warning, "Detect unusual transactions") to "anomalies"
            )

            items(quickActions) { (action, route) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { navController.navigate("ai_$route") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(action.second, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(action.first, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text(action.third, fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = VyaparTextSecondary)
                    }
                }
            }

            if (uiState.recentInsights.isNotEmpty()) {
                item {
                    Text(
                        "Recent Insights",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(uiState.recentInsights.take(5)) { insight ->
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
                                Text(insight.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = when (insight.impact) {
                                        "positive" -> VyaparGreen.copy(alpha = 0.1f)
                                        "negative" -> VyaparRed.copy(alpha = 0.1f)
                                        else -> VyaparBlue.copy(alpha = 0.1f)
                                    }
                                ) {
                                    Text(
                                        insight.impact.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (insight.impact) {
                                            "positive" -> VyaparGreen
                                            "negative" -> VyaparRed
                                            else -> VyaparBlue
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(insight.description, fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

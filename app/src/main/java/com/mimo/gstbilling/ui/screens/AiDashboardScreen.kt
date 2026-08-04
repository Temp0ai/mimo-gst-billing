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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.AiDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDashboardScreen(navController: NavController, viewModel: AiDashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    data class AiFeature(val title: String, val subtitle: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)

    val features = listOf(
        AiFeature("Smart Reminders", "Payment due alerts", Icons.Filled.Notifications, Screen.AiSmartReminders.route),
        AiFeature("Cash Flow Forecast", "Predict future cash flow", Icons.Filled.TrendingUp, Screen.AiCashFlow.route),
        AiFeature("Tax Advisor", "GST/Tax saving tips", Icons.Filled.Calculate, Screen.AiTaxAdvisor.route),
        AiFeature("Business Insights", "KPI analytics", Icons.Filled.Analytics, Screen.AiInsights.route),
        AiFeature("Duplicate Check", "Find duplicates", Icons.Filled.ContentCopy, Screen.AiDuplicates.route),
        AiFeature("Anomaly Detection", "Unusual patterns", Icons.Filled.Warning, Screen.AiAnomalies.route),
        AiFeature("Smart Pricing", "Optimal price suggestions", Icons.Filled.AttachMoney, Screen.SmartPricing.route),
        AiFeature("Inventory Reorder", "Stockout prediction", Icons.Filled.Inventory, Screen.InventoryReorder.route),
        AiFeature("Customer Churn", "At-risk customers", Icons.Filled.People, Screen.CustomerChurn.route),
        AiFeature("Sales Trend", "Forecast sales", Icons.Filled.ShowChart, Screen.SalesTrend.route),
        AiFeature("Party Risk Score", "Payment reliability", Icons.Filled.Security, Screen.PartyRisk.route),
        AiFeature("GST Filing", "Deadline reminders", Icons.Filled.Event, Screen.GstFiling.route),
        AiFeature("Expense Optimizer", "Cost-cutting tips", Icons.Filled.Savings, Screen.ExpenseOptimizer.route),
        AiFeature("Business Health", "Overall health score", Icons.Filled.Favorite, Screen.BusinessHealth.route),
        AiFeature("Invoice Suggestions", "Smart item suggestions", Icons.Filled.Receipt, Screen.SmartInvoiceSuggest.route),
        AiFeature("Payment Patterns", "Detect payment habits", Icons.Filled.QueryStats, Screen.PaymentPattern.route),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Dashboard", fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Default, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparBlue)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("AI Business Assistant", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Intelligent insights for your business", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column { Text("${uiState.insightsCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White); Text("Insights", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f)) }
                            Column { Text("${uiState.alertsCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White); Text("Alerts", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f)) }
                            Column { Text("${uiState.suggestionsCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White); Text("Suggestions", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f)) }
                        }
                    }
                }
            }

            item { Text("All AI Features (${features.size})", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }

            items(features) { feature ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { navController.navigate(feature.route) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(feature.icon, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(feature.title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary, fontFamily = FontFamily.Default)
                            Text(feature.subtitle, fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Default)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (uiState.recentInsights.isNotEmpty()) {
                item { Text("Recent Insights", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
                items(uiState.recentInsights.take(5)) { insight ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(insight.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Surface(shape = RoundedCornerShape(50), color = when (insight.impact) { "positive" -> VyaparGreen.copy(alpha = 0.1f); "negative" -> VyaparRed.copy(alpha = 0.1f); else -> VyaparBlue.copy(alpha = 0.1f) }) {
                                    Text(insight.impact.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = when (insight.impact) { "positive" -> VyaparGreen; "negative" -> VyaparRed; else -> VyaparBlue })
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(insight.description, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

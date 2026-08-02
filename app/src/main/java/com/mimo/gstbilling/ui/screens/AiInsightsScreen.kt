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
import com.mimo.gstbilling.ui.viewmodel.AiInsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    navController: NavController,
    viewModel: AiInsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Business Insights",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightBlueBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                uiState.metrics?.let { metrics ->
                    BusinessMetricsCard(
                        totalRevenue = metrics.totalRevenue,
                        totalExpenses = metrics.totalExpenses,
                        profitMargin = metrics.profitMargin,
                        averageOrderValue = metrics.averageOrderValue
                    )
                }
            }

            item {
                Text(
                    text = "Top Customers",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (uiState.topCustomers.isNotEmpty()) {
                items(uiState.topCustomers) { customer ->
                    TopCustomerItem(
                        name = customer.partyName,
                        totalAmount = customer.totalAmount,
                        invoiceCount = customer.invoiceCount,
                        averageOrderValue = customer.averageOrderValue
                    )
                }
            } else {
                item {
                    EmptyStateCard(message = "No customer data available yet")
                }
            }

            item {
                Text(
                    text = "Sales Trends",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (uiState.salesTrends.isNotEmpty()) {
                items(uiState.salesTrends) { trend ->
                    SalesTrendItem(
                        period = trend.period,
                        amount = trend.amount,
                        changePercent = trend.changePercent
                    )
                }
            } else {
                item {
                    EmptyStateCard(message = "No sales trend data available")
                }
            }

            item {
                Text(
                    text = "Business Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (uiState.insights.isNotEmpty()) {
                items(uiState.insights) { insight ->
                    InsightItem(
                        title = insight.title,
                        description = insight.description,
                        metric = insight.metric,
                        trend = insight.trend,
                        impact = insight.impact
                    )
                }
            } else {
                item {
                    EmptyStateCard(message = "No insights available yet")
                }
            }

            item {
                Button(
                    onClick = { viewModel.refreshInsights() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VyaparBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refresh Insights",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BusinessMetricsCard(
    totalRevenue: Double,
    totalExpenses: Double,
    profitMargin: Double,
    averageOrderValue: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = VyaparBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Business Metrics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    label = "Total Revenue",
                    value = "₹${String.format("%.0f", totalRevenue)}",
                    color = VyaparGreen
                )
                MetricColumn(
                    label = "Expenses",
                    value = "₹${String.format("%.0f", totalExpenses)}",
                    color = VyaparRed
                )
                MetricColumn(
                    label = "Profit Margin",
                    value = "${String.format("%.1f", profitMargin)}%",
                    color = if (profitMargin >= 20) VyaparGreen else RedAccent
                )
                MetricColumn(
                    label = "Avg Order",
                    value = "₹${String.format("%.0f", averageOrderValue)}",
                    color = VyaparBlue
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = VyaparTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TopCustomerItem(
    name: String,
    totalAmount: Double,
    invoiceCount: Int,
    averageOrderValue: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = VyaparBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$invoiceCount invoices",
                    fontSize = 12.sp,
                    color = VyaparTextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.0f", totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyaparGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Avg: ₹${String.format("%.0f", averageOrderValue)}",
                    fontSize = 11.sp,
                    color = VyaparTextSecondary
                )
            }
        }
    }
}

@Composable
private fun SalesTrendItem(
    period: String,
    amount: Double,
    changePercent: Double
) {
    val isPositive = changePercent >= 0
    val trendColor = if (isPositive) VyaparGreen else RedAccent
    val trendIcon = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    .background(trendColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = period,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "₹${String.format("%.0f", amount)}",
                    fontSize = 13.sp,
                    color = VyaparTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = trendColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.1f", changePercent)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = trendColor
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightItem(
    title: String,
    description: String,
    metric: String,
    trend: String,
    impact: String
) {
    val impactColor = when (impact) {
        "positive" -> VyaparGreen
        "negative" -> RedAccent
        else -> VyaparBlue
    }
    val trendIcon = when (trend) {
        "up" -> Icons.Default.TrendingUp
        "down" -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = impactColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = impactColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = impact.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = impactColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = VyaparTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = LightBlueBg
            ) {
                Text(
                    text = metric,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = VyaparBlue
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = VyaparTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = VyaparTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

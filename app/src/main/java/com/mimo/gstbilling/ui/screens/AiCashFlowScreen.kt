package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.LightBlueBg
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.VyaparBlue
import com.mimo.gstbilling.ui.theme.VyaparGreen
import com.mimo.gstbilling.ui.theme.VyaparRed
import com.mimo.gstbilling.ui.theme.VyaparTextSecondary
import com.mimo.gstbilling.ui.viewmodel.AiCashFlowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCashFlowScreen(
    navController: NavController,
    viewModel: AiCashFlowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cash Flow Forecast",
                        fontWeight = FontWeight.SemiBold
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
                    containerColor = MaterialTheme.colorScheme.surface
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                uiState.summary?.let { summary ->
                    SummaryCard(
                        currentBalance = summary.currentBalance,
                        monthlyAverageIncome = summary.monthlyAverageIncome,
                        monthlyAverageExpenses = summary.monthlyAverageExpenses,
                        runwayMonths = summary.runwayMonths
                    )
                }
            }

            item {
                ChartSection(
                    monthlyForecasts = uiState.forecasts
                )
            }

            item {
                Text(
                    text = "Monthly Forecasts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            items(uiState.forecasts) { forecast ->
                MonthlyForecastItem(forecast = forecast)
            }

            item {
                Button(
                    onClick = { viewModel.generateForecast() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VyaparBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Forecast",
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
private fun SummaryCard(
    currentBalance: Double,
    monthlyAverageIncome: Double,
    monthlyAverageExpenses: Double,
    runwayMonths: Int
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
                    imageVector = Icons.Default.Balance,
                    contentDescription = null,
                    tint = VyaparBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Financial Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Current Balance",
                fontSize = 12.sp,
                color = VyaparTextSecondary
            )
            Text(
                text = "₹${String.format("%.2f", currentBalance)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Avg Income",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "₹${String.format("%.0f", monthlyAverageIncome)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VyaparGreen
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Avg Expenses",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "₹${String.format("%.0f", monthlyAverageExpenses)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VyaparRed
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Runway",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "$runwayMonths months",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartSection(
    monthlyForecasts: List<com.mimo.gstbilling.utils.AiCashFlowForecaster.CashFlowForecast>
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
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = VyaparBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Income vs Expenses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyForecasts.takeLast(6).forEach { forecast ->
                    val maxAmount = maxOf(forecast.projectedIncome, forecast.projectedExpenses)
                        .coerceAtLeast(1.0)
                    val incomeHeight = (forecast.projectedIncome / maxAmount * 120).dp
                    val expenseHeight = (forecast.projectedExpenses / maxAmount * 120).dp

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.height(120.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(incomeHeight)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(VyaparGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(expenseHeight)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(VyaparRed)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = forecast.month.take(3),
                            fontSize = 10.sp,
                            color = VyaparTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(VyaparGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Income",
                    fontSize = 12.sp,
                    color = VyaparTextSecondary
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(VyaparRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Expenses",
                    fontSize = 12.sp,
                    color = VyaparTextSecondary
                )
            }
        }
    }
}

@Composable
private fun MonthlyForecastItem(
    forecast: com.mimo.gstbilling.utils.AiCashFlowForecaster.CashFlowForecast
) {
    val trendColor = when {
        forecast.netCashFlow > 0 -> VyaparGreen
        forecast.netCashFlow < 0 -> VyaparRed
        else -> VyaparTextSecondary
    }

    val trendIcon = when {
        forecast.netCashFlow > 0 -> Icons.Default.TrendingUp
        forecast.netCashFlow < 0 -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }

    val trendLabel = when {
        forecast.netCashFlow > 0 -> "Positive"
        forecast.netCashFlow < 0 -> "Negative"
        else -> "Stable"
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
                Text(
                    text = forecast.month,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trendLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = trendColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Income",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "₹${String.format("%.0f", forecast.projectedIncome)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VyaparGreen
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expenses",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "₹${String.format("%.0f", forecast.projectedExpenses)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VyaparRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Cash Flow",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "₹${String.format("%.0f", forecast.netCashFlow)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = if (forecast.projectedIncome > 0) {
                (forecast.projectedExpenses / forecast.projectedIncome).coerceIn(0.0, 1.0).toFloat()
            } else {
                0f
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Expense Ratio",
                        fontSize = 11.sp,
                        color = VyaparTextSecondary
                    )
                    Text(
                        text = "${String.format("%.1f", progress * 100)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress > 0.9f) VyaparRed else VyaparGreen,
                    trackColor = LightBlueBg
                )
            }
        }
    }
}

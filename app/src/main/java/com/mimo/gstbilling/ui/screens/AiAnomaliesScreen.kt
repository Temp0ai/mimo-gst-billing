package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mimo.gstbilling.ui.viewmodel.AiAnomaliesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnomaliesScreen(
    navController: NavController,
    viewModel: AiAnomaliesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Anomaly Detection",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBlueBg
                )
            )
        },
        containerColor = LightBlueBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                RiskScoreCard(
                    riskScore = uiState.riskScore,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                AnomalySummaryCard(
                    highCount = uiState.highCount,
                    mediumCount = uiState.mediumCount,
                    lowCount = uiState.lowCount,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(uiState.anomalies) { anomaly ->
                AnomalyCard(
                    anomaly = anomaly,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                RunDetectionButton(
                    isRunning = uiState.isScanning,
                    onClick = { viewModel.runDetection() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun RiskScoreCard(
    riskScore: Int,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        riskScore <= 33 -> VyaparGreen
        riskScore <= 66 -> Color(0xFFFF9800)
        else -> VyaparRed
    }
    val scoreLabel = when {
        riskScore <= 33 -> "Low Risk"
        riskScore <= 66 -> "Medium Risk"
        else -> "High Risk"
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Risk Score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scoreColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = scoreLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { riskScore / 100f },
                        modifier = Modifier.size(120.dp),
                        color = scoreColor,
                        trackColor = scoreColor.copy(alpha = 0.15f),
                        strokeWidth = 12.dp
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$riskScore",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = "out of 100",
                            fontSize = 12.sp,
                            color = VyaparTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnomalySummaryCard(
    highCount: Int,
    mediumCount: Int,
    lowCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Anomaly Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    count = highCount,
                    label = "High",
                    color = VyaparRed,
                    icon = Icons.Filled.Error
                )
                SummaryItem(
                    count = mediumCount,
                    label = "Medium",
                    color = Color(0xFFFF9800),
                    icon = Icons.Filled.Warning
                )
                SummaryItem(
                    count = lowCount,
                    label = "Low",
                    color = VyaparGreen,
                    icon = Icons.Filled.Info
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$count",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = VyaparTextSecondary
        )
    }
}

@Composable
private fun AnomalyCard(
    anomaly: AiAnomaly,
    modifier: Modifier = Modifier
) {
    val severityColor = when (anomaly.severity) {
        "high" -> VyaparRed
        "medium" -> Color(0xFFFF9800)
        "low" -> VyaparGreen
        else -> VyaparTextSecondary
    }
    val severityLabel = when (anomaly.severity) {
        "high" -> "High"
        "medium" -> "Medium"
        "low" -> "Low"
        else -> "Unknown"
    }
    val typeIcon = when (anomaly.type) {
        "tax_mismatch" -> Icons.Filled.AccountBalance
        "amount_discrepancy" -> Icons.Filled.AttachMoney
        "duplicate_entry" -> Icons.Filled.Group
        "unusual_pattern" -> Icons.Filled.ShowChart
        "threshold_breach" -> Icons.Filled.PriorityHigh
        "frequency_anomaly" -> Icons.Filled.BarChart
        "data_inconsistency" -> Icons.Filled.ErrorOutline
        "fraud_indicator" -> Icons.Filled.BugReport
        else -> Icons.Filled.HelpOutline
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = anomaly.type,
                        tint = VyaparBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = anomaly.type.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = severityColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = severityLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = severityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = anomaly.description,
                fontSize = 14.sp,
                color = VyaparTextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = LightBlueBg,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Detected Value",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = anomaly.detectedValue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = VyaparRed
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Expected Range",
                        fontSize = 12.sp,
                        color = VyaparTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = anomaly.expectedRange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = VyaparGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun RunDetectionButton(
    isRunning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = !isRunning,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = VyaparBlue,
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
            disabledContentColor = VyaparBlue.copy(alpha = 0.5f)
        )
    ) {
        if (isRunning) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = VyaparBlue,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Scanning...",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Run Detection",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.AiTaxAdvisorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTaxAdvisorScreen(
    navController: NavController,
    viewModel: AiTaxAdvisorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tax Advisor",
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                GssSummaryCard(
                    cgst = uiState.cgst,
                    sgst = uiState.sgst,
                    igst = uiState.igst,
                    inputCredit = uiState.inputCredit,
                    netPayable = uiState.netPayable
                )
            }

            item {
                Text(
                    text = "Tax Saving Suggestions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(uiState.suggestions) { suggestion ->
                TaxSuggestionCard(suggestion = suggestion)
            }

            item {
                Text(
                    text = "Input Tax Credit Opportunities",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(uiState.creditOpportunities) { opportunity ->
                CreditOpportunityCard(opportunity = opportunity)
            }

            item {
                Button(
                    onClick = { viewModel.analyzeTaxSavings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VyaparGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyze Tax Savings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GssSummaryCard(
    cgst: Double,
    sgst: Double,
    igst: Double,
    inputCredit: Double,
    netPayable: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "GST Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "CGST", amount = cgst, color = VyaparBlue)
                SummaryItem(label = "SGST", amount = sgst, color = VyaparGreen)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "IGST", amount = igst, color = VyaparTextSecondary)
                SummaryItem(label = "Input Credit", amount = inputCredit, color = VyaparBlue)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = LightBlueBg, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Payable",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "₹%.2f".format(netPayable),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (netPayable > 0) VyaparRed else VyaparGreen
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, amount: Double, color: Color) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = VyaparTextSecondary
        )
        Text(
            text = "₹%.2f".format(amount),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun TaxSuggestionCard(suggestion: TaxSuggestion) {
    val priorityColor = when (suggestion.priority) {
        "High" -> VyaparRed
        "Medium" -> Color(0xFFFF9800)
        "Low" -> VyaparGreen
        else -> VyaparTextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = priorityColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = suggestion.priority,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = priorityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = suggestion.description,
                fontSize = 14.sp,
                color = VyaparTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VyaparBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = suggestion.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = VyaparBlue
                    )
                }

                Text(
                    text = "Save ₹%.0f".format(suggestion.potentialSavings),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyaparGreen
                )
            }
        }
    }
}

@Composable
private fun CreditOpportunityCard(opportunity: CreditOpportunity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyaparBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = VyaparBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = opportunity.vendor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = opportunity.description,
                    fontSize = 12.sp,
                    color = VyaparTextSecondary
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "₹%.2f".format(opportunity.amount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyaparGreen
                )
                Text(
                    text = opportunity.invoiceNumber,
                    fontSize = 12.sp,
                    color = VyaparTextSecondary
                )
            }
        }
    }
}

data class TaxSuggestion(
    val title: String,
    val description: String,
    val potentialSavings: Double,
    val category: String,
    val priority: String
)

data class CreditOpportunity(
    val vendor: String,
    val description: String,
    val amount: Double,
    val invoiceNumber: String
)

data class AiTaxAdvisorUiState(
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val igst: Double = 0.0,
    val inputCredit: Double = 0.0,
    val netPayable: Double = 0.0,
    val suggestions: List<TaxSuggestion> = emptyList(),
    val creditOpportunities: List<CreditOpportunity> = emptyList(),
    val isLoading: Boolean = false
)

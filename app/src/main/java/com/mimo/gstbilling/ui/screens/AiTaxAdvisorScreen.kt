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
                title = { Text("Tax Advisor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GST Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("CGST", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text("₹${String.format("%.2f", uiState.gstSummary["cgst"] ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VyaparBlue)
                            }
                            Column {
                                Text("SGST", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text("₹${String.format("%.2f", uiState.gstSummary["sgst"] ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VyaparGreen)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("IGST", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text("₹${String.format("%.2f", uiState.gstSummary["igst"] ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VyaparTextSecondary)
                            }
                            Column {
                                Text("Input Credit", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text("₹${String.format("%.2f", uiState.gstSummary["inputCredit"] ?: 0.0)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VyaparBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = LightBlueBg, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Net Payable", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            val netPayable = uiState.gstSummary["netPayable"] ?: 0.0
                            Text("₹${String.format("%.2f", netPayable)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (netPayable > 0) VyaparRed else VyaparGreen)
                        }
                    }
                }
            }

            item {
                Text("Tax Saving Suggestions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(uiState.suggestions) { suggestion ->
                val priorityColor = when (suggestion.priority) {
                    "High" -> VyaparRed
                    "Medium" -> Color(0xFFFF9800)
                    "Low" -> VyaparGreen
                    else -> VyaparTextSecondary
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(suggestion.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(8.dp), color = priorityColor.copy(alpha = 0.1f)) {
                                Text(suggestion.priority, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = priorityColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(suggestion.description, fontSize = 14.sp, color = VyaparTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = VyaparBlue.copy(alpha = 0.1f)) {
                                Text(suggestion.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, color = VyaparBlue)
                            }
                            Text("Save ₹${String.format("%.0f", suggestion.potentialSavings)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.analyzeTaxSavings() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Tax Savings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

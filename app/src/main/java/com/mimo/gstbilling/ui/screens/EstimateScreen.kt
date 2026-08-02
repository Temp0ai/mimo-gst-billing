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
import com.mimo.gstbilling.data.local.entity.EstimateEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.EstimateViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimateScreen(navController: NavController, viewModel: EstimateViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estimates", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        if (uiState.estimates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(80.dp), tint = VyaparTextSecondary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No estimates yet", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = VyaparTextSecondary)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(uiState.estimates) { estimate ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(estimate.estimateNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Surface(shape = RoundedCornerShape(50), color = when(estimate.status) {
                                    "accepted" -> VyaparGreen.copy(alpha = 0.1f)
                                    "rejected" -> VyaparRed.copy(alpha = 0.1f)
                                    "expired" -> Color.Gray.copy(alpha = 0.1f)
                                    else -> VyaparBlue.copy(alpha = 0.1f)
                                }) {
                                    Text(estimate.status.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                        color = when(estimate.status) { "accepted" -> VyaparGreen; "rejected" -> VyaparRed; "expired" -> Color.Gray; else -> VyaparBlue })
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(estimate.partyName, fontSize = 14.sp, color = VyaparTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column { Text("Date", fontSize = 11.sp, color = VyaparTextSecondary); Text(dateFormat.format(Date(estimate.date)), fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                                Column(horizontalAlignment = Alignment.End) { Text("Valid Until", fontSize = 11.sp, color = VyaparTextSecondary); Text(dateFormat.format(Date(estimate.validUntil)), fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                                Column(horizontalAlignment = Alignment.End) { Text("Amount", fontSize = 11.sp, color = VyaparTextSecondary); Text("₹${String.format("%,.2f", estimate.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VyaparBlue) }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (estimate.status == "pending") {
                                    AssistChip(onClick = { viewModel.updateEstimateStatus(estimate.id, "accepted") }, label = { Text("Convert to Invoice", fontSize = 11.sp) }, leadingIcon = { Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(16.dp)) })
                                    AssistChip(onClick = { viewModel.updateEstimateStatus(estimate.id, "rejected") }, label = { Text("Reject", fontSize = 11.sp) }, leadingIcon = { Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp)) })
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

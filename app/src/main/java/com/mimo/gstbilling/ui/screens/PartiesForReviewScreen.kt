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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class PartyReview(
    val id: Long, val name: String, val phone: String?, val gstin: String?, val date: Long, val status: String = "pending"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesForReviewScreen(navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var parties by remember { mutableStateOf(listOf(
        PartyReview(1, "New Supplier Co.", "9876543210", "27AABCU9603R1ZM", System.currentTimeMillis() - 86400000, "pending"),
        PartyReview(2, "Quick Services", "9988776655", null, System.currentTimeMillis() - 86400000 * 2, "pending"),
        PartyReview(3, "Metro Traders", "9112233445", "09AABCM1234F1Z5", System.currentTimeMillis() - 86400000 * 3, "approved"),
        PartyReview(4, "Unverified Party", "9001122334", "INVALIDGSTIN", System.currentTimeMillis(), "rejected")
    )) }
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filtered = when(selectedFilter) { 1 -> parties.filter { it.status == "pending" }; 2 -> parties.filter { it.status == "approved" }; 3 -> parties.filter { it.status == "rejected" }; else -> parties }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Parties for Review", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Pending", "Approved", "Rejected").forEachIndexed { index, label ->
                    FilterChip(selected = selectedFilter == index, onClick = { selectedFilter = index }, label = { Text(label, fontSize = 12.sp) })
                }
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filtered) { party ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(party.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Surface(shape = RoundedCornerShape(50), color = when(party.status) { "approved" -> VyaparGreen.copy(alpha = 0.1f); "rejected" -> VyaparRed.copy(alpha = 0.1f); else -> Color(0xFFFFF3E0) }) {
                                    Text(party.status.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = when(party.status) { "approved" -> VyaparGreen; "rejected" -> VyaparRed; else -> Color(0xFFE65100) })
                                }
                            }
                            if (party.phone != null) Text("Phone: ${party.phone}", fontSize = 13.sp, color = VyaparTextSecondary)
                            if (party.gstin != null) Text("GSTIN: ${party.gstin}", fontSize = 13.sp, color = VyaparTextSecondary)
                            Text("Added: ${dateFormat.format(Date(party.date))}", fontSize = 12.sp, color = VyaparTextSecondary)
                            if (party.status == "pending") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { parties = parties.map { if (it.id == party.id) it.copy(status = "approved") else it } }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = VyaparGreen), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) { Text("Approve", color = Color.White, fontSize = 12.sp) }
                                    OutlinedButton(onClick = { parties = parties.map { if (it.id == party.id) it.copy(status = "rejected") else it } }, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) { Text("Reject", fontSize = 12.sp) }
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

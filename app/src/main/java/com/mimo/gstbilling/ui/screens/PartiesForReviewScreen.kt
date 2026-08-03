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
import com.mimo.gstbilling.data.local.entity.PartyEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.PartyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesForReviewScreen(
    navController: NavController,
    partyViewModel: PartyViewModel = hiltViewModel()
) {
    val parties by partyViewModel.parties.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var selectedFilter by remember { mutableIntStateOf(0) }

    val filtered = when (selectedFilter) {
        1 -> parties.filter { it.gstin.isNullOrBlank() }
        2 -> parties.filter { !it.gstin.isNullOrBlank() }
        else -> parties
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties for Review", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Without GSTIN", "With GSTIN").forEachIndexed { index, label ->
                    FilterChip(selected = selectedFilter == index, onClick = { selectedFilter = index }, label = { Text(label, fontSize = 12.sp) })
                }
            }
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All parties reviewed!", fontSize = 16.sp, color = TextPrimary)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { party ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(party.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Surface(shape = RoundedCornerShape(50), color = if (!party.gstin.isNullOrBlank()) VyaparGreen.copy(alpha = 0.1f) else Color(0xFFFFF3E0)) {
                                        Text(
                                            if (!party.gstin.isNullOrBlank()) "GSTIN OK" else "NO GSTIN",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                            color = if (!party.gstin.isNullOrBlank()) VyaparGreen else Color(0xFFE65100)
                                        )
                                    }
                                }
                                if (!party.phone.isNullOrBlank()) Text("Phone: ${party.phone}", fontSize = 13.sp, color = VyaparTextSecondary)
                                if (!party.gstin.isNullOrBlank()) Text("GSTIN: ${party.gstin}", fontSize = 13.sp, color = VyaparTextSecondary)
                                if (!party.address.isNullOrBlank()) Text("Address: ${party.address}", fontSize = 12.sp, color = VyaparTextSecondary)
                                Text("Added: ${dateFormat.format(Date(party.createdAt))}", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPartyBulkSendScreen(
    navController: NavController,
    partyViewModel: PartyViewModel = hiltViewModel()
) {
    val parties by partyViewModel.parties.collectAsState()
    val selectedIds = remember { mutableStateListOf<Long>() }
    var messageType by remember { mutableStateOf("invoice") }
    val messageTypes = listOf("invoice" to "Invoice", "reminder" to "Payment Reminder", "greeting" to "Greeting", "offer" to "Offer")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bulk Send", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Message Type", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        messageTypes.forEach { (type, label) ->
                            FilterChip(selected = messageType == type, onClick = { messageType = type }, label = { Text(label, fontSize = 12.sp) })
                        }
                    }
                }
            }
            Text("${selectedIds.size} parties selected", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 13.sp, color = VyaparBlue, fontWeight = FontWeight.Medium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(parties) { party ->
                    val isSelected = party.id in selectedIds
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSelected, onCheckedChange = { checked -> if (checked) selectedIds.add(party.id) else selectedIds.remove(party.id) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(party.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(party.phone ?: "No phone", fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                enabled = selectedIds.isNotEmpty()
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send to ${selectedIds.size} parties", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

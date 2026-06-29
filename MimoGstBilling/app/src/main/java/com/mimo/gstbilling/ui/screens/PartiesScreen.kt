package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val parties = remember { mutableStateListOf(
        Party("ABC Enterprises", "9876543210", "GSTIN1234567890", "Customer", "Rs. 12,450 Cr"),
        Party("XYZ Traders", "8765432109", "GSTIN0987654321", "Supplier", "Rs. 5,200 Dr"),
        Party("Global Solutions", "7654321098", "GSTIN1122334455", "Customer", "Rs. 25,000 Cr"),
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Secondary) {
                Icon(Icons.Default.Add, contentDescription = "Add Party")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                label = { Text("Search parties") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(parties.size) { index ->
                    val party = parties[index]
                    PartyCard(party)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddPartyDialog(onDismiss = { showAddDialog = false }, onAdd = { name, phone, gstin, type ->
            parties.add(Party(name, phone, gstin, type, "Rs. 0.00"))
            showAddDialog = false
        })
    }
}

@Composable
private fun PartyCard(party: Party) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = "Party", tint = Primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(party.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(party.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(party.gstin ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (party.type == "Customer") {
                    Text(party.type, style = MaterialTheme.typography.bodySmall, color = Success)
                } else {
                    Text(party.type, style = MaterialTheme.typography.bodySmall, color = Error)
                }
            }
            Text(party.balance, fontWeight = FontWeight.Bold, color = if (party.balance.contains("Cr")) Success else Error)
        }
    }
}

@Composable
private fun AddPartyDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Customer") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Party") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = gstin, onValueChange = { gstin = it }, label = { Text("GSTIN") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, phone, gstin, type) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

data class Party(val name: String, val phone: String, val gstin: String?, val type: String, val balance: String)

package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class Party(
    val name: String,
    val balance: String,
    val date: String,
    val isPositive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showMoreOptions by remember { mutableStateOf(false) }

    val parties = remember {
        mutableStateListOf(
            Party("dignitary defence academy", "22,570.00", "19 Jun 26"),
            Party("Sha Khimji & Premji Co Market Yard", "21,100.00", "02 Jun 26"),
            Party("Sanman enterprises", "1,15,227.00", "09 May 26"),
            Party("Adv. pramod Bendre", "36,640.00", "27 Apr 26"),
            Party("Agad logistics & supply chain", "6,380.00", "20 Apr 26"),
            Party("Ishwar Heart Clinic", "370.00", "16 Apr 26"),
            Party("Taste of irani", "7,350.00", "15 Mar 26"),
            Party("Ibhraim Himly", "4,93,240.00", "07 Mar 26"),
            Party("Pramod thakkar", "550.00", "16 Feb 26"),
            Party("Sg promotors", "2,050.00", "10 Feb 26"),
            Party("Digital task force", "5,504.00", "05 Feb 26")
        )
    }

    val filteredParties = parties.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Parties", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreOptions = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlueHeader)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = RedAccent,
                contentColor = OnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add new party")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Parties") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Party List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(filteredParties.size) { index ->
                    val party = filteredParties[index]
                    PartyListItem(party)
                    if (index < filteredParties.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }

    // More Options Bottom Sheet
    if (showMoreOptions) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptions = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "More Options",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    headlineContent = { Text("Bulk Payment Reminder") },
                    leadingContent = { Icon(Icons.Default.Payment, contentDescription = null) },
                    modifier = Modifier.clickable { showMoreOptions = false }
                )
                ListItem(
                    headlineContent = { Text("Bulk Message") },
                    leadingContent = { Icon(Icons.Default.Message, contentDescription = null) },
                    modifier = Modifier.clickable { showMoreOptions = false }
                )
                ListItem(
                    headlineContent = { Text("Sort by Name [A-Z]") },
                    leadingContent = { Icon(Icons.Default.Sort, contentDescription = null) },
                    trailingContent = { Checkbox(checked = false, onCheckedChange = null) },
                    modifier = Modifier.clickable { showMoreOptions = false }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PartyListItem(party: Party) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                party.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                party.date,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "\u20B9 ${party.balance}",
                fontWeight = FontWeight.Bold,
                color = GreenBalance
            )
            Text(
                "You'll Get",
                style = MaterialTheme.typography.bodySmall,
                color = GreenBalance
            )
        }
    }
}

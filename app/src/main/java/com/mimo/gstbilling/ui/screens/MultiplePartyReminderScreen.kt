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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

data class PartyReminder(
    val id: Long,
    val name: String,
    val outstanding: Double,
    var isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplePartyReminderScreen(navController: NavController) {
    var selectAll by remember { mutableStateOf(false) }
    val parties = remember {
        mutableStateListOf(
            PartyReminder(1, "Rahul Enterprises", 15000.0),
            PartyReminder(2, "Priya Traders", 8500.0),
            PartyReminder(3, "Amit & Sons", 22000.0),
            PartyReminder(4, "Neha Distributors", 4200.0),
            PartyReminder(5, "Vikram Supply Co.", 31000.0),
            PartyReminder(6, "Sonia Retail", 6800.0)
        )
    }

    val selectedCount = parties.count { it.isSelected }
    val totalOutstanding = parties.filter { it.isSelected }.sumOf { it.outstanding }

    LaunchedEffect(selectAll) {
        parties.forEach { it.isSelected = selectAll }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Selected: $selectedCount parties", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            "Total: \u20B9${String.format("%,.0f", totalOutstanding)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (selectedCount > 0) {
                                navController.navigate(Screen.InputReminderMessage.route)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = selectedCount > 0
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Reminders ($selectedCount)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectAll,
                        onCheckedChange = { selectAll = it },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Text("Select All", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                }
            }

            items(parties) { party ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (party.isSelected) VyaparLightBlue else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = party.isSelected,
                            onCheckedChange = {
                                party.isSelected = it
                                selectAll = parties.all { p -> p.isSelected }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(party.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Outstanding: \u20B9${String.format("%,.0f", party.outstanding)}",
                                fontSize = 13.sp,
                                color = RedAccent
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
        }
    }
}

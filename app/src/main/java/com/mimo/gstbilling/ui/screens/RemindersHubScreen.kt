package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersHubScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Overdue", "Completed")

    val upcomingReminders = listOf(
        Triple("Rajesh Traders", "₹25,000", "Due: 28 Jul 2026"),
        Triple("Meera Textiles", "₹22,000", "Due: 30 Jul 2026")
    )
    val overdueReminders = listOf(
        Triple("Suresh & Sons", "₹8,500", "Overdue: 22 Jul 2026"),
        Triple("Verma Auto Parts", "₹6,500", "Overdue: 24 Jul 2026")
    )
    val completedReminders = listOf(
        Triple("Kumar Plumbing", "₹8,500", "Completed: 20 Jul 2026")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* notification settings */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* add reminder */ },
                containerColor = Primary,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Primary)
                    }
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = {
                            Text(tab, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Primary else TextSecondary)
                        })
                    }
                }
            }

            val currentReminders = when (selectedTab) {
                0 -> upcomingReminders
                1 -> overdueReminders
                else -> completedReminders
            }

            if (currentReminders.isEmpty()) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No ${tabs[selectedTab].lowercase()} reminders", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                currentReminders.forEach { (party, amount, status) ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(
                                when (selectedTab) {
                                    1 -> RedAccent.copy(alpha = 0.1f)
                                    2 -> GreenBalance.copy(alpha = 0.1f)
                                    else -> Primary.copy(alpha = 0.1f)
                                }
                            ), contentAlignment = Alignment.Center) {
                                Icon(
                                    when (selectedTab) {
                                        1 -> Icons.Filled.Warning
                                        2 -> Icons.Filled.CheckCircle
                                        else -> Icons.Filled.Schedule
                                    },
                                    contentDescription = null,
                                    tint = when (selectedTab) { 1 -> RedAccent; 2 -> GreenBalance; else -> Primary },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(party, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                Text(status, fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(amount, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

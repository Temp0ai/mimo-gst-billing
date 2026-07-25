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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class SentSms(val id: Long, val recipient: String, val message: String, val timestamp: String)
data class SmsTemplate(val id: Long, val name: String, val message: String)
data class ScheduledSms(val id: Long, val recipient: String, val message: String, val scheduledTime: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsListScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Sent", "Templates", "Scheduled")

    val sentMessages = remember {
        listOf(
            SentSms(1, "9876543210", "Dear customer, your invoice #INV-001 is ready. Amount: \u20B95,000", "25 Jul 10:30 AM"),
            SentSms(2, "9876543211", "Payment reminder: \u20B93,500 due from your account.", "24 Jul 02:15 PM"),
            SentSms(3, "9876543212", "Thank you for your purchase! Your order #ORD-003 is confirmed.", "23 Jul 11:45 AM")
        )
    }

    val templates = remember {
        listOf(
            SmsTemplate(1, "Invoice Ready", "Dear {party_name}, your invoice #{invoice_number} is ready. Amount: {amount}"),
            SmsTemplate(2, "Payment Reminder", "Dear {party_name}, this is a reminder for pending payment of {amount}."),
            SmsTemplate(3, "Order Confirmed", "Thank you {party_name}! Your order #{order_number} has been confirmed."),
            SmsTemplate(4, "Payment Received", "Dear {party_name}, we have received your payment of {amount}. Thank you!")
        )
    }

    val scheduledMessages = remember {
        listOf(
            ScheduledSms(1, "9876543210", "Payment reminder for invoice #INV-005", "26 Jul 09:00 AM"),
            ScheduledSms(2, "9876543211", "Monthly statement for July 2026", "31 Jul 10:00 AM")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Primary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Primary
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Primary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sentMessages) { sms ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Send, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(sms.recipient, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        }
                                        Text(sms.timestamp, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(sms.message, fontSize = 13.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(templates) { template ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(template.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Row {
                                            IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Primary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(template.message, fontSize = 13.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (scheduledMessages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = VyaparEmptyStateIcon, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No scheduled messages", fontSize = 16.sp, color = VyaparEmptyStateText)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(scheduledMessages) { sms ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(sms.recipient, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                            Text(sms.scheduledTime, fontSize = 11.sp, color = VyaparStatusOrange)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(sms.message, fontSize = 13.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

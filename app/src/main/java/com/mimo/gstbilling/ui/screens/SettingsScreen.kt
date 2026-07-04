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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

data class VyaparSettingsItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color = Primary,
    val hasNew: Boolean = false,
    val isPremium: Boolean = false,
    val subItems: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var expandedSection by remember { mutableStateOf("") }

    val settingsItems = listOf(
        VyaparSettingsItem("General", Icons.Filled.Settings, Primary, hasNew = true,
            subItems = listOf("Business Profile", "Invoice Settings", "Item Settings", "Party Settings")),
        VyaparSettingsItem("Transaction", Icons.Filled.CurrencyRupee, Color(0xFF4CAF50), hasNew = true,
            subItems = listOf("Transaction Settings", "Payment Settings")),
        VyaparSettingsItem("Invoice Print", Icons.Filled.Print, Primary,
            subItems = listOf("Print Format", "Print Size")),
        VyaparSettingsItem("Taxes & GST", Icons.Filled.Percent, RedAccent, hasNew = true,
            subItems = listOf("Tax Configuration", "TCS/TDS Settings")),
        VyaparSettingsItem("User Management", Icons.Filled.Group, Color(0xFF9C27B0),
            subItems = listOf("Add Staff", "Manage Permissions")),
        VyaparSettingsItem("Transaction SMS", Icons.Filled.Chat, Color(0xFF2196F3), hasNew = true,
            subItems = listOf("SMS Templates", "Auto Send")),
        VyaparSettingsItem("Reminders", Icons.Filled.Notifications, Color(0xFFFF9800),
            subItems = listOf("Payment Reminders", "Stock Alerts")),
        VyaparSettingsItem("Party", Icons.Filled.People, Color(0xFF607D8B),
            subItems = listOf("Party Settings", "Party Groups")),
        VyaparSettingsItem("Item", Icons.Filled.Inventory, Color(0xFF00BCD4),
            subItems = listOf("Item Settings", "Units & Categories")),
        VyaparSettingsItem("Multi-Currency", Icons.Filled.AttachMoney, Color(0xFF795548), isPremium = true,
            subItems = listOf("Currency Settings"))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            items(settingsItems.size) { index ->
                val item = settingsItems[index]
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedSection = if (expandedSection == item.title) "" else item.title
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(item.title, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        if (item.hasNew) {
                            Box(
                                modifier = Modifier
                                    .background(RedAccent, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("NEW", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (item.isPremium) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFCDD2), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PRO", fontSize = 10.sp, color = RedAccent, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            if (expandedSection == item.title) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    if (expandedSection == item.title) {
                        item.subItems.forEach { subItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (subItem) {
                                            "Business Profile" -> navController.navigate(Screen.BusinessProfile.route)
                                            "Transaction Settings" -> { }
                                            "Payment Settings" -> { }
                                            "Tax Configuration" -> { }
                                            "TCS/TDS Settings" -> { }
                                            "Party Groups" -> navController.navigate(Screen.PartyGroups.route)
                                            "Payment Reminders" -> navController.navigate(Screen.PaymentReminders.route)
                                        }
                                    }
                                    .background(Color(0xFFF8F9FA))
                                    .padding(start = 56.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(subItem, fontSize = 14.sp, color = TextPrimary)
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

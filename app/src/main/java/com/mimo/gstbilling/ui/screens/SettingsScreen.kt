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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.ThemeManager
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.ui.theme.LightBlueBg

data class SettingsItem(val title: String, val subtitle: String = "", val icon: ImageVector, val iconColor: Color = Primary, val hasToggle: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkMode by ThemeManager.isDarkMode

    val generalSettings = listOf(
        SettingsItem("Business Profile", "Company name, address, GSTIN", Icons.Filled.Business, Primary),
        SettingsItem("Invoice Settings", "Format, prefix, numbering", Icons.Filled.Description, Color(0xFFFF9800)),
        SettingsItem("Transaction Settings", "Payment terms, rounding", Icons.Filled.Receipt, GreenBalance),
        SettingsItem("Item Settings", "Units, categories, stock", Icons.Filled.Inventory, Color(0xFF00BCD4)),
        SettingsItem("Party Settings", "Groups, payment reminders", Icons.Filled.Group, Color(0xFF9C27B0))
    )
    val taxSettings = listOf(
        SettingsItem("Tax Configuration", "GST rates, HSN codes", Icons.Filled.Receipt, RedAccent),
        SettingsItem("TCS/TDS Settings", "Tax collection at source", Icons.Filled.Receipt, Color(0xFF795548))
    )
    val appSettings = listOf(
        SettingsItem("Notifications", "", Icons.Filled.Notifications, Primary, hasToggle = true),
        SettingsItem("Dark Mode", "", Icons.Filled.Settings, Color(0xFF455A64), hasToggle = true),
        SettingsItem("Security", "App lock, biometric", Icons.Filled.Lock, Color(0xFFE91E63)),
        SettingsItem("Backup & Restore", "Local backup", Icons.Filled.CloudDownload, Primary),
        SettingsItem("Thermal Printer", "Bluetooth printer setup", Icons.Filled.Print, GreenBalance)
    )
    val aboutSettings = listOf(
        SettingsItem("About Mimo GST", "Version 1.0.0", Icons.Filled.Info, Primary),
        SettingsItem("Privacy Policy", "", Icons.Filled.Info, TextSecondary),
        SettingsItem("Terms of Service", "", Icons.Filled.Info, TextSecondary)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            fun settingsSection(title: String) { /* handled inline */ }
            item { Text(title = "General", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            items(generalSettings) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp).clickable {
                    when (item.title) {
                        "Party Settings" -> navController.navigate(Screen.PartyGroups.route)
                    }
                }, shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) { Text(item.title, fontSize = 15.sp, color = TextPrimary); if (item.subtitle.isNotEmpty()) Text(item.subtitle, fontSize = 12.sp, color = TextSecondary) }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }
            item { Text("Tax", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            items(taxSettings) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) { Text(item.title, fontSize = 15.sp, color = TextPrimary); if (item.subtitle.isNotEmpty()) Text(item.subtitle, fontSize = 12.sp, color = TextSecondary) }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }
            item { Text("App", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            items(appSettings) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp).clickable {
                    when (item.title) {
                        "Backup & Restore" -> navController.navigate(Screen.BackupRestore.route)
                        "Thermal Printer" -> navController.navigate(Screen.ThermalPrinter.route)
                    }
                }, shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(item.title, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        if (item.hasToggle) {
                            Switch(checked = if (item.title == "Notifications") notificationsEnabled else darkMode, onCheckedChange = { if (item.title == "Notifications") notificationsEnabled = it else darkMode = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                        } else {
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }
            item { Text("About", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            items(aboutSettings) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) { Text(item.title, fontSize = 15.sp, color = TextPrimary); if (item.subtitle.isNotEmpty()) Text(item.subtitle, fontSize = 12.sp, color = TextSecondary) }
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

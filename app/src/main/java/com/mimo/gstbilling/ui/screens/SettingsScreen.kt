package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary

data class SettingsItem(
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val iconColor: Color = Primary,
    val hasToggle: Boolean = false,
    val toggleDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    val generalSettings = listOf(
        SettingsItem("Business Profile", "Company name, address, GSTIN", Icons.Filled.Business, Primary),
        SettingsItem("Invoice Settings", "Format, prefix, numbering", Icons.Filled.Description, Color(0xFFFF9800)),
        SettingsItem("Transaction Settings", "Payment terms, rounding", Icons.Filled.Receipt, Color(0xFF4CAF50)),
        SettingsItem("Item Settings", "Units, categories, stock", Icons.Filled.Inventory, Color(0xFF00BCD4)),
        SettingsItem("Party Settings", "Groups, payment reminders", Icons.Filled.Group, Color(0xFF9C27B0))
    )

    val taxSettings = listOf(
        SettingsItem("Tax Configuration", "GST rates, HSN codes", Icons.Filled.Receipt, RedAccent),
        SettingsItem("TCS/TDS Settings", "Tax collection at source", Icons.Filled.Receipt, Color(0xFF795548))
    )

    val appSettings = listOf(
        SettingsItem("Notifications", "", Icons.Filled.Notifications, Primary, hasToggle = true, toggleDefault = true),
        SettingsItem("Dark Mode", "", Icons.Filled.Settings, Color(0xFF455A64), hasToggle = true, toggleDefault = false),
        SettingsItem("Security", "App lock, biometric", Icons.Filled.Security, Color(0xFFE91E63)),
        SettingsItem("Backup & Restore", "Local & Google Drive", Icons.Filled.FileDownload, Primary)
    )

    val aboutSettings = listOf(
        SettingsItem("About Mimo GST", "Version 1.0.0", Icons.Filled.Info, Primary),
        SettingsItem("Privacy Policy", "", Icons.Filled.Info, TextSecondary),
        SettingsItem("Terms of Service", "", Icons.Filled.Info, TextSecondary)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            item { SettingsSectionHeader("General") }
            items(generalSettings.size) { index ->
                SettingsRow(generalSettings[index])
            }

            item { SettingsSectionHeader("Tax") }
            items(taxSettings.size) { index ->
                SettingsRow(taxSettings[index])
            }

            item { SettingsSectionHeader("App") }
            items(appSettings.size) { index ->
                val item = appSettings[index]
                if (item.hasToggle) {
                    SettingsToggleRow(
                        item = item,
                        checked = if (item.title == "Notifications") notificationsEnabled else darkMode,
                        onCheckedChange = {
                            if (item.title == "Notifications") notificationsEnabled = it else darkMode = it
                        }
                    )
                } else {
                    SettingsRow(item)
                }
            }

            item { SettingsSectionHeader("About") }
            items(aboutSettings.size) { index ->
                SettingsRow(aboutSettings[index])
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsRow(item: SettingsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 15.sp, color = TextPrimary)
                if (item.subtitle.isNotEmpty()) {
                    Text(item.subtitle, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
    }
}

@Composable
fun SettingsToggleRow(item: SettingsItem, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(item.title, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Primary)
            )
        }
        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
    }
}

private val RedAccent = Color(0xFFE53935)

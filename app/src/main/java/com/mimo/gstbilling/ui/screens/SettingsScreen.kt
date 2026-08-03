package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.navigation.VyaparBottomBar
import com.mimo.gstbilling.ui.theme.*

data class SettingsCategory(
    val title: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val items: List<SettingsSubItem>
)

data class SettingsSubItem(
    val title: String,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var expandedSection by remember { mutableStateOf("") }
    val isDarkMode by ThemeManager.isDarkMode

    val categories = listOf(
        SettingsCategory("General", Icons.Filled.Settings, Color(0xFFE3F2FD), VyaparBlue, listOf(
            SettingsSubItem("Business Profile", Screen.BusinessProfile.route),
            SettingsSubItem("Switch Company", Screen.CompanySwitch.route),
            SettingsSubItem("Invoice Settings", Screen.InvoiceSettings.route),
            SettingsSubItem("Item Settings", Screen.ItemSettings.route),
            SettingsSubItem("Party Settings", Screen.PartySettings.route),
            SettingsSubItem("Biometric Lock", Screen.BiometricSettings.route)
        )),
        SettingsCategory("Transaction", Icons.Filled.CurrencyRupee, Color(0xFFE8F5E9), Color(0xFF4CAF50), listOf(
            SettingsSubItem("Transaction Settings", Screen.TransactionSettings.route),
            SettingsSubItem("Payment Settings", Screen.PaymentTerms.route)
        )),
        SettingsCategory("Invoice Print", Icons.Filled.Print, Color(0xFFE3F2FD), VyaparBlue, listOf(
            SettingsSubItem("Print Format", Screen.PrintFormat.route),
            SettingsSubItem("Print Size", Screen.PrintFormat.route)
        )),
        SettingsCategory("Taxes & GST", Icons.Filled.Percent, Color(0xFFFCE4EC), RedAccent, listOf(
            SettingsSubItem("Tax Configuration", Screen.TaxSettings.route),
            SettingsSubItem("TCS/TDS Settings", Screen.TaxSettings.route)
        )),
        SettingsCategory("User Management", Icons.Filled.Group, Color(0xFFF3E5F5), Color(0xFF9C27B0), listOf(
            SettingsSubItem("Add Staff", Screen.StaffSettings.route),
            SettingsSubItem("Manage Permissions", Screen.Permissions.route)
        )),
        SettingsCategory("Transaction SMS", Icons.Filled.Chat, Color(0xFFE3F2FD), Color(0xFF2196F3), listOf(
            SettingsSubItem("SMS Templates", Screen.SmsTemplates.route),
            SettingsSubItem("Auto Send", Screen.AutoSend.route)
        )),
        SettingsCategory("Reminders", Icons.Filled.Notifications, Color(0xFFFFF3E0), Color(0xFFFF9800), listOf(
            SettingsSubItem("Payment Reminders", Screen.PaymentReminders.route),
            SettingsSubItem("Stock Alerts", Screen.StockAlerts.route)
        )),
        SettingsCategory("Party", Icons.Filled.People, Color(0xFFECEFF1), Color(0xFF607D8B), listOf(
            SettingsSubItem("Party Groups", Screen.PartyGroups.route)
        )),
        SettingsCategory("Item", Icons.Filled.Inventory, Color(0xFFE0F7FA), Color(0xFF00BCD4), listOf(
            SettingsSubItem("Units & Categories", Screen.UnitsCategories.route)
        )),
        SettingsCategory("Data", Icons.Filled.Storage, Color(0xFFECEFF1), Color(0xFF455A64), listOf(
            SettingsSubItem("Import Database", Screen.ImportData.route),
            SettingsSubItem("Export Database", Screen.ExportData.route),
            SettingsSubItem("Backup & Restore", Screen.BackupRestore.route)
        )),
        SettingsCategory("Operations", Icons.Filled.Work, Color(0xFFE8EAF6), Color(0xFF5C6BC0), listOf(
            SettingsSubItem("Staff Management", Screen.StaffManagement.route),
            SettingsSubItem("Delivery Tracking", Screen.DeliveryTracking.route),
            SettingsSubItem("Expense Approval", Screen.ExpenseApproval.route),
            SettingsSubItem("Discount Configuration", Screen.DiscountConfig.route)
        )),
        SettingsCategory("Business Tools", Icons.Filled.Build, Color(0xFFFFF3E0), Color(0xFFEF6C00), listOf(
            SettingsSubItem("Analytics Dashboard", Screen.AnalyticsDashboard.route),
            SettingsSubItem("Item Price List", Screen.ItemPriceList.route),
            SettingsSubItem("Manufacturing", Screen.Manufacturing.route),
            SettingsSubItem("Orders", Screen.Orders.route),
            SettingsSubItem("Bank Reconciliation", Screen.BankReconciliation.route),
            SettingsSubItem("GSTR-9 Return", Screen.Gstr9.route)
        ))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val selectedTab = when (currentRoute) {
                Screen.Dashboard.route -> 0
                Screen.Parties.route -> 1
                Screen.Items.route -> 3
                Screen.Settings.route -> 4
                else -> 4
            }
            VyaparBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    val targetRoute = when (tab) {
                        0 -> Screen.Dashboard.route
                        1 -> Screen.Parties.route
                        3 -> Screen.Items.route
                        4 -> Screen.Settings.route
                        else -> Screen.Dashboard.route
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddClick = { navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "sales")) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .clickable { ThemeManager.toggleDarkMode() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEDE7F6)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.DarkMode, contentDescription = null, tint = Color(0xFF6750A4), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("Dark Mode", fontSize = 15.sp, color = TextPrimary, fontFamily = FontFamily.SansSerif, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { ThemeManager.toggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedTrackColor = VyaparBlue, checkedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFE0E0E0))
                    )
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
            }

            categories.forEach { category ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .clickable { expandedSection = if (expandedSection == category.title) "" else category.title }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(category.iconBg), contentAlignment = Alignment.Center) {
                            Icon(category.icon, contentDescription = null, tint = category.iconTint, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(category.title, fontSize = 15.sp, color = TextPrimary, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(
                            if (expandedSection == category.title) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                            contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)
                        )
                    }

                    if (expandedSection == category.title) {
                        category.items.forEach { subItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8F9FA))
                                    .clickable {
                                        navController.navigate(subItem.route)
                                    }
                                    .padding(start = 70.dp, end = 16.dp, top = 13.dp, bottom = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(VyaparBlue.copy(alpha = 0.3f)))
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(subItem.title, fontSize = 14.sp, color = TextPrimary, fontFamily = FontFamily.SansSerif)
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

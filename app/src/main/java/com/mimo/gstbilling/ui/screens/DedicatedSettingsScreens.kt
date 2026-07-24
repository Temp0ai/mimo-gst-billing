package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("invoice_settings", Context.MODE_PRIVATE)
    var autoIncrement by remember { mutableStateOf(prefs.getBoolean("auto_increment", true)) }
    var prefix by remember { mutableStateOf(prefs.getString("prefix", "") ?: "") }
    var startingNumber by remember { mutableStateOf(prefs.getInt("starting_number", 1)) }
    var defaultDueDays by remember { mutableStateOf(prefs.getInt("default_due_days", 30)) }
    var showPartyDetails by remember { mutableStateOf(prefs.getBoolean("show_party_details", true)) }
    var showBankDetails by remember { mutableStateOf(prefs.getBoolean("show_bank_details", true)) }
    var showDeclaration by remember { mutableStateOf(prefs.getBoolean("show_declaration", true)) }
    var showQrCode by remember { mutableStateOf(prefs.getBoolean("show_qr_code", true)) }
    var showTermsConditions by remember { mutableStateOf(prefs.getBoolean("show_terms_conditions", true)) }
    var showSignature by remember { mutableStateOf(prefs.getBoolean("show_signature", false)) }
    var defaultPaymentMode by remember { mutableStateOf(prefs.getString("default_payment_mode", "Credit") ?: "Credit") }

    fun save(key: String, value: Any) {
        prefs.edit().apply {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is String -> putString(key, value)
            }
            apply()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    Text("Numbering", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    SettingToggleRow("Auto Increment Invoice Number", autoIncrement) { autoIncrement = it; save("auto_increment", it) }
                    OutlinedTextField(value = prefix, onValueChange = { prefix = it; save("prefix", it) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), label = { Text("Invoice Prefix (e.g. INV-)") }, shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Starting Number", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (startingNumber > 1) { startingNumber--; save("starting_number", startingNumber) } }) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary) }
                            Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(startingNumber.toString(), fontWeight = FontWeight.Bold, color = Primary) }
                            }
                            IconButton(onClick = { startingNumber++; save("starting_number", startingNumber) }) { Icon(Icons.Filled.Add, contentDescription = null, tint = Primary) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    Text("Defaults", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Default Payment Mode", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Row {
                            listOf("Credit", "Cash").forEach { mode ->
                                FilterChip(selected = defaultPaymentMode == mode, onClick = { defaultPaymentMode = mode; save("default_payment_mode", mode) }, label = { Text(mode) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White))
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Default Due Days", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (defaultDueDays > 1) { defaultDueDays--; save("default_due_days", defaultDueDays) } }) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary) }
                            Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(defaultDueDays.toString(), fontWeight = FontWeight.Bold, color = Primary) }
                            }
                            IconButton(onClick = { defaultDueDays++; save("default_due_days", defaultDueDays) }) { Icon(Icons.Filled.Add, contentDescription = null, tint = Primary) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    Text("Invoice Display", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    SettingToggleRow("Show Party Details", showPartyDetails) { showPartyDetails = it; save("show_party_details", it) }
                    SettingToggleRow("Show Bank Details", showBankDetails) { showBankDetails = it; save("show_bank_details", it) }
                    SettingToggleRow("Show Declaration", showDeclaration) { showDeclaration = it; save("show_declaration", it) }
                    SettingToggleRow("Show QR Code", showQrCode) { showQrCode = it; save("show_qr_code", it) }
                    SettingToggleRow("Show Terms & Conditions", showTermsConditions) { showTermsConditions = it; save("show_terms_conditions", it) }
                    SettingToggleRow("Show Signature", showSignature) { showSignature = it; save("show_signature", it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintFormatScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("print_settings", Context.MODE_PRIVATE)
    var selectedFormat by remember { mutableStateOf(prefs.getString("print_format", "A4") ?: "A4") }

    val formats = listOf(
        Triple("A4", "Standard A4 (210mm x 297mm)", "Full-page invoice with all details"),
        Triple("Thermal 80mm", "80mm thermal paper", "Compact receipt format"),
        Triple("Thermal 58mm", "58mm thermal paper", "Small receipt format"),
        Triple("A5", "Half A4 (148mm x 210mm)", "Smaller format for mini invoices")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Print Format", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            formats.forEach { (name, size, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedFormat = name; prefs.edit().putString("print_format", name).apply() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedFormat == name) Primary.copy(alpha = 0.1f) else Color(0xFFF5F5F5)),
                    border = if (selectedFormat == name) CardDefaults.outlinedCardBorder(enabled = true) else null
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Print, contentDescription = null, tint = if (selectedFormat == name) Primary else TextSecondary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text(size, fontSize = 12.sp, color = TextSecondary)
                            Text(desc, fontSize = 11.sp, color = TextSecondary)
                        }
                        if (selectedFormat == name) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("staff_settings", Context.MODE_PRIVATE)
    var staffCount by remember { mutableStateOf(prefs.getInt("staff_count", 0)) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Staff", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Staff Members", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add staff to create login accounts with limited access", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (staffCount == 0) {
                        Icon(Icons.Filled.People, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No staff members added yet", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        repeat(staffCount) { i ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp)) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Staff Member ${i + 1}", modifier = Modifier.weight(1f), color = TextPrimary)
                                    Text("Admin", fontSize = 12.sp, color = Primary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { staffCount++; prefs.edit().putInt("staff_count", staffCount).apply() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Staff Member")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("permissions_settings", Context.MODE_PRIVATE)

    data class PermItem(val label: String, val key: String, val default: Boolean)
    val permissions = listOf(
        PermItem("View Sales", "view_sales", true),
        PermItem("Create Sales", "create_sales", true),
        PermItem("Delete Sales", "delete_sales", false),
        PermItem("View Purchases", "view_purchases", true),
        PermItem("Create Purchases", "create_purchases", true),
        PermItem("View Parties", "view_parties", true),
        PermItem("Create Parties", "create_parties", true),
        PermItem("View Items", "view_items", true),
        PermItem("Create Items", "create_items", true),
        PermItem("View Reports", "view_reports", true),
        PermItem("View Settings", "view_settings", false),
        PermItem("Manage Settings", "manage_settings", false),
        PermItem("View Expenses", "view_expenses", true),
        PermItem("Create Expenses", "create_expenses", true),
        PermItem("View Cash/Bank", "view_cashbank", true)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Permissions", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Staff Member Permissions", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    permissions.forEach { perm ->
                        var checked by remember { mutableStateOf(prefs.getBoolean(perm.key, perm.default)) }
                        SettingToggleRow(perm.label, checked) { checked = it; prefs.edit().putBoolean(perm.key, it).apply() }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsTemplatesScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("sms_settings", Context.MODE_PRIVATE)
    var enabled by remember { mutableStateOf(prefs.getBoolean("sms_enabled", false)) }
    var welcomeMsg by remember { mutableStateOf(prefs.getString("welcome_template", "Welcome {party_name}! Thank you for your business.") ?: "") }
    var paymentReminder by remember { mutableStateOf(prefs.getString("payment_reminder", "Dear {party_name}, your payment of {amount} is due. Please pay at your earliest.") ?: "") }
    var thankYouMsg by remember { mutableStateOf(prefs.getString("thank_you_template", "Thank you for your payment of {amount}! Your balance is {balance}.") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SMS Templates", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column { SettingToggleRow("Enable SMS Notifications", enabled) { enabled = it; prefs.edit().putBoolean("sms_enabled", it).apply() } }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Variables: {party_name}, {amount}, {balance}, {invoice_number}, {due_date}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
            listOf("Welcome Message" to (welcomeMsg to "welcome_template"), "Payment Reminder" to (paymentReminder to "payment_template"), "Thank You" to (thankYouMsg to "thank_you_template")).forEach { (title, tpl) ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = tpl.first, onValueChange = { prefs.edit().putString(tpl.second, it).apply() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSendScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auto_send_settings", Context.MODE_PRIVATE)
    var autoSendInvoice by remember { mutableStateOf(prefs.getBoolean("auto_send_invoice", false)) }
    var autoSendReminder by remember { mutableStateOf(prefs.getBoolean("auto_send_reminder", false)) }
    var reminderDays by remember { mutableStateOf(prefs.getInt("reminder_days_before", 3)) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Auto Send", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    SettingToggleRow("Auto-send Invoice via WhatsApp", autoSendInvoice) { autoSendInvoice = it; prefs.edit().putBoolean("auto_send_invoice", it).apply() }
                    SettingToggleRow("Auto-send Payment Reminders", autoSendReminder) { autoSendReminder = it; prefs.edit().putBoolean("auto_send_reminder", it).apply() }
                    if (autoSendReminder) {
                        HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Remind days before due date", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (reminderDays > 1) { reminderDays--; prefs.edit().putInt("reminder_days_before", reminderDays).apply() } }) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary) }
                                Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(reminderDays.toString(), fontWeight = FontWeight.Bold, color = Primary) }
                                }
                                IconButton(onClick = { reminderDays++; prefs.edit().putInt("reminder_days_before", reminderDays).apply() }) { Icon(Icons.Filled.Add, contentDescription = null, tint = Primary) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAlertsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("stock_alerts", Context.MODE_PRIVATE)
    var lowStockAlert by remember { mutableStateOf(prefs.getBoolean("low_stock_alert", true)) }
    var threshold by remember { mutableStateOf(prefs.getInt("low_stock_threshold", 10)) }
    var expiryAlert by remember { mutableStateOf(prefs.getBoolean("expiry_alert", false)) }
    var expiryDays by remember { mutableStateOf(prefs.getInt("expiry_days_before", 30)) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Stock Alerts", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    SettingToggleRow("Low Stock Alert", lowStockAlert) { lowStockAlert = it; prefs.edit().putBoolean("low_stock_alert", it).apply() }
                    if (lowStockAlert) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Alert when stock below", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (threshold > 1) { threshold--; prefs.edit().putInt("low_stock_threshold", threshold).apply() } }) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary) }
                                Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(threshold.toString(), fontWeight = FontWeight.Bold, color = Primary) }
                                }
                                IconButton(onClick = { threshold++; prefs.edit().putInt("low_stock_threshold", threshold).apply() }) { Icon(Icons.Filled.Add, contentDescription = null, tint = Primary) }
                            }
                        }
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingToggleRow("Expiry Date Alert", expiryAlert) { expiryAlert = it; prefs.edit().putBoolean("expiry_alert", it).apply() }
                    if (expiryAlert) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Alert days before expiry", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (expiryDays > 1) { expiryDays--; prefs.edit().putInt("expiry_days_before", expiryDays).apply() } }) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary) }
                                Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(expiryDays.toString(), fontWeight = FontWeight.Bold, color = Primary) }
                                }
                                IconButton(onClick = { expiryDays++; prefs.edit().putInt("expiry_days_before", expiryDays).apply() }) { Icon(Icons.Filled.Add, contentDescription = null, tint = Primary) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsCategoriesScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("units_settings", Context.MODE_PRIVATE)
    var units by remember { mutableStateOf((prefs.getStringSet("units", setOf("Pcs", "Kg", "Ltr", "Mtr", "Box", "Bag", "Set", "Pair")) ?: setOf("Pcs", "Kg", "Ltr", "Mtr", "Box", "Bag", "Set", "Pair")).toMutableList()) }
    var newUnit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Units & Categories", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).padding(16.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Measurement Units", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = newUnit, onValueChange = { newUnit = it }, modifier = Modifier.weight(1f), label = { Text("Add new unit") }, shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary))
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { if (newUnit.isNotBlank()) { units.add(newUnit.trim()); newUnit = ""; prefs.edit().putStringSet("units", units.toSet()).apply() } }, shape = RoundedCornerShape(50)) { Text("Add") }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    units.forEach { unit ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Straighten, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(unit, modifier = Modifier.weight(1f), color = TextPrimary)
                            IconButton(onClick = { units.remove(unit); prefs.edit().putStringSet("units", units.toSet()).apply() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = RedAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (unit != units.last()) HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("currency_settings", Context.MODE_PRIVATE)
    var selectedCurrency by remember { mutableStateOf(prefs.getString("currency", "INR") ?: "INR") }
    var symbolPosition by remember { mutableStateOf(prefs.getString("symbol_position", "before") ?: "before") }
    var decimalPlaces by remember { mutableStateOf(prefs.getInt("decimal_places", 2)) }

    val currencies = listOf("INR" to "Indian Rupee (\u20B9)", "USD" to "US Dollar ($)", "EUR" to "Euro (\u20AC)", "GBP" to "British Pound (\u00A3)", "AED" to "UAE Dirham (AED)", "SAR" to "Saudi Riyal (SAR)")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Currency Settings", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Currency", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    currencies.forEach { (code, name) ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { selectedCurrency = code; prefs.edit().putString("currency", code).apply() }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedCurrency == code, onClick = { selectedCurrency = code; prefs.edit().putString("currency", code).apply() })
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(name, color = TextPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Symbol Position", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Row {
                            listOf("Before" to "before", "After" to "after").forEach { (label, value) ->
                                FilterChip(selected = symbolPosition == value, onClick = { symbolPosition = value; prefs.edit().putString("symbol_position", value).apply() }, label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Decimal Places", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (decimalPlaces > 0) { decimalPlaces--; prefs.edit().putInt("decimal_places", decimalPlaces).apply() } }) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary) }
                            Card(modifier = Modifier.width(50.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(decimalPlaces.toString(), fontWeight = FontWeight.Bold, color = Primary) }
                            }
                            IconButton(onClick = { if (decimalPlaces < 4) { decimalPlaces++; prefs.edit().putInt("decimal_places", decimalPlaces).apply() } }) { Icon(Icons.Filled.Add, contentDescription = null, tint = Primary) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    val sym = if (selectedCurrency == "INR") "\u20B9" else selectedCurrency
                    val pos = if (symbolPosition == "before") "${sym}1,234.00" else "1,234.00$sym"
                    Text("Preview: $pos", fontSize = 13.sp, color = TextPrimary)
                }
            }
        }
    }
}

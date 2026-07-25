package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("tax_settings", Context.MODE_PRIVATE)

    var gst by remember { mutableStateOf(prefs.getBoolean("gst", true)) }
    var hsnSacCode by remember { mutableStateOf(prefs.getBoolean("hsn_sac_code", true)) }
    var additionalCess by remember { mutableStateOf(prefs.getBoolean("additional_cess", false)) }
    var reverseCharge by remember { mutableStateOf(prefs.getBoolean("reverse_charge", false)) }
    var stateOfSupply by remember { mutableStateOf(prefs.getBoolean("state_of_supply", true)) }
    var ewayBillNo by remember { mutableStateOf(prefs.getBoolean("eway_bill_no", true)) }
    var compositeScheme by remember { mutableStateOf(prefs.getBoolean("composite_scheme", false)) }
    var enableTcs by remember { mutableStateOf(prefs.getBoolean("enable_tcs", false)) }
    var enableTds by remember { mutableStateOf(prefs.getBoolean("enable_tds", false)) }

    fun save(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taxes & GST", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Show search/filter for tax settings */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            SettingNavigationRow("Tax List", false) { navController.navigate(Screen.SettingsDetail.createRoute("Tax List")) }

            SettingToggleRow("GST", gst) { gst = it; save("gst", it) }
            SettingToggleRow("HSN/SAC Code", hsnSacCode) { hsnSacCode = it; save("hsn_sac_code", it) }
            SettingToggleRow("Additional CESS", additionalCess) { additionalCess = it; save("additional_cess", it) }
            SettingToggleRow("Reverse Charge", reverseCharge) { reverseCharge = it; save("reverse_charge", it) }
            SettingToggleRow("State of Supply", stateOfSupply) { stateOfSupply = it; save("state_of_supply", it) }
            SettingToggleRow("E-Way Bill No.", ewayBillNo) { ewayBillNo = it; save("eway_bill_no", it) }
            SettingToggleRow("Composite Scheme", compositeScheme) { compositeScheme = it; save("composite_scheme", it) }
            SettingToggleRow("Enable TCS", enableTcs) { enableTcs = it; save("enable_tcs", it) }
            SettingToggleRow("Enable TDS", enableTds) { enableTds = it; save("enable_tds", it) }
        }
    }
}

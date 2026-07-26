package com.mimo.gstbilling.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartySettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("party_settings", Context.MODE_PRIVATE)
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var gstinNumber by remember { mutableStateOf(prefs.getBoolean("gstin_number", true)) }
    var partyGrouping by remember { mutableStateOf(prefs.getBoolean("party_grouping", true)) }
    var partyShippingAddress by remember { mutableStateOf(prefs.getBoolean("party_shipping_address", true)) }
    var printShippingAddress by remember { mutableStateOf(prefs.getBoolean("print_shipping_address", true)) }
    var inviteParties by remember { mutableStateOf(prefs.getBoolean("invite_parties", true)) }
    var loyaltyPoints by remember { mutableStateOf(prefs.getBoolean("loyalty_points", true)) }

    fun save(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Party", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
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
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search party settings") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = { IconButton(onClick = { showSearch = false; searchQuery = "" }) { Icon(Icons.Filled.Close, contentDescription = "Close") } },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            SettingToggleRow("GSTIN Number", gstinNumber) { gstinNumber = it; save("gstin_number", it) }
            SettingToggleRow("Party Grouping", partyGrouping) { partyGrouping = it; save("party_grouping", it) }

            SettingNavigationRow("Party Additional Fields", false) { navController.navigate(Screen.SettingsDetail.createRoute("Party Additional Fields")) }

            SettingToggleRow("Party Shipping Address", partyShippingAddress) { partyShippingAddress = it; save("party_shipping_address", it) }
            SettingToggleRow("Print Shipping Address", printShippingAddress) { printShippingAddress = it; save("print_shipping_address", it) }
            SettingToggleRow("Invite parties to add themselves", inviteParties) { inviteParties = it; save("invite_parties", it) }
            SettingToggleRow("Loyalty Points", loyaltyPoints) { loyaltyPoints = it; save("loyalty_points", it) }
        }
    }
}

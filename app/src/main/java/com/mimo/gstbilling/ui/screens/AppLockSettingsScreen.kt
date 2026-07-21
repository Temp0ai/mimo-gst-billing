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
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.utils.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var biometricEnabled by remember { mutableStateOf(BiometricHelper.isBiometricLockEnabled(context)) }
    Scaffold(topBar = { TopAppBar(title = { Text("App Lock", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Background).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text("Fingerprint Lock", fontSize = 15.sp, color = TextPrimary); Text("Require fingerprint to open app", fontSize = 11.sp, color = TextSecondary) }
                        Switch(checked = biometricEnabled, onCheckedChange = { BiometricHelper.setBiometricLockEnabled(context, it); biometricEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

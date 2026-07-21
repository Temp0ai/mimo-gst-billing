package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("email_settings", Context.MODE_PRIVATE)
    var smtpServer by remember { mutableStateOf(prefs.getString("smtp", "") ?: "") }
    var smtpPort by remember { mutableStateOf(prefs.getString("port", "587") ?: "587") }
    var senderEmail by remember { mutableStateOf(prefs.getString("sender", "") ?: "") }
    var senderPassword by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    fun save(key: String, value: String) { prefs.edit().putString(key, value).apply() }

    Scaffold(topBar = { TopAppBar(title = { Text("Email Settings", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Background).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SMTP Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    OutlinedTextField(value = smtpServer, onValueChange = { smtpServer = it; save("smtp", it) }, label = { Text("SMTP Server") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = smtpPort, onValueChange = { smtpPort = it; save("port", it) }, label = { Text("Port") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = senderEmail, onValueChange = { senderEmail = it; save("sender", it) }, label = { Text("Sender Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = senderPassword, onValueChange = { senderPassword = it; save("password", it) }, label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Save Settings", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

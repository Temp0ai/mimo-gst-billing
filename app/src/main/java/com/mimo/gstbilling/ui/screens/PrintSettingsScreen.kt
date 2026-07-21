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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun PrintSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("print_settings", Context.MODE_PRIVATE)
    var printFormat by remember { mutableStateOf(prefs.getString("format", "A4") ?: "A4") }
    var showCopies by remember { mutableStateOf(prefs.getInt("copies", 1)) }
    var showFormatDropdown by remember { mutableStateOf(false) }
    fun save(key: String, value: Any) { when (value) { is String -> prefs.edit().putString(key, value).apply(); is Int -> prefs.edit().putInt(key, value).apply() } }

    Scaffold(topBar = { TopAppBar(title = { Text("Print Settings", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Background).verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Print Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Box { OutlinedTextField(value = printFormat, onValueChange = {}, readOnly = true, label = { Text("Print Format") }, trailingIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.clickable { showFormatDropdown = true }) }, modifier = Modifier.fillMaxWidth()); DropdownMenu(expanded = showFormatDropdown, onDismissRequest = { showFormatDropdown = false }) { listOf("A4", "A5", "Thermal 80mm", "Thermal 58mm").forEach { f -> DropdownMenuItem(text = { Text(f) }, onClick = { printFormat = f; save("format", f); showFormatDropdown = false }) } } }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Text("Default Copies", fontSize = 14.sp, color = TextPrimary); Row { IconButton(onClick = { if (showCopies > 1) { showCopies--; save("copies", showCopies) } }) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary) }; Text(showCopies.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); IconButton(onClick = { showCopies++; save("copies", showCopies) }) { Icon(Icons.Filled.Add, contentDescription = null, tint = Primary) } } }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

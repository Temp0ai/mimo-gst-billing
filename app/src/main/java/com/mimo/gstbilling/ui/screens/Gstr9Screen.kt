package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gstr9Screen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("GSTR-9 Annual Return", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("GSTR-9 Annual Return", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary); Spacer(modifier = Modifier.height(8.dp)); Text("Annual GST return filing for the financial year.", fontSize = 13.sp, color = TextSecondary) } } }
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { GstrRow("Total Outward Supplies", 0.0); GstrRow("Total Inward Supplies (ITC)", 0.0); GstrRow("Tax Paid", 0.0); GstrRow("Late Fees", 0.0) } } }
            item { Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Generate GSTR-9 JSON", fontWeight = FontWeight.Bold) } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun GstrRow(label: String, amount: Double) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, fontSize = 13.sp, color = TextSecondary); Text(String.format(java.util.Locale.US, "\u20B9%,.2f", amount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary) } }

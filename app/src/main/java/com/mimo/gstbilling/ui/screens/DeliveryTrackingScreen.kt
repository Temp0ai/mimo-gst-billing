package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
fun DeliveryTrackingScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Delivery Tracking", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("Delivery Tracking", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary); Spacer(modifier = Modifier.height(8.dp)); Text("Track and manage deliveries for your orders.", fontSize = 13.sp, color = TextSecondary) } } }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = GreenBalance.copy(alpha = 0.1f))) { Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(4.dp)); Text("Pending", fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))) { Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(4.dp)); Text("Delivered", fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = RedAccent.copy(alpha = 0.1f))) { Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Cancel, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.height(4.dp)); Text("Returned", fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
                        }
                    }
                }
            }
            item { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("No deliveries to track", color = TextSecondary) } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

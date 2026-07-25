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
fun SubscriptionScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Subscription", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f))) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Current Plan", fontSize = 14.sp, color = TextSecondary); Text("Free", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Primary) } } }
            listOf(Triple("Silver", "\u20B9499/mo", listOf("Unlimited Invoices", "5 Users", "Basic Reports", "Email Support")), Triple("Gold", "\u20B9999/mo", listOf("Everything in Silver", "25 Users", "Advanced Reports", "WhatsApp Integration", "Priority Support")), Triple("Platinum", "\u20B91999/mo", listOf("Everything in Gold", "Unlimited Users", "All Reports", "API Access", "Dedicated Support", "Custom Branding"))).forEach { (plan, price, features) ->
                item { Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text(plan, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary); Text(price, fontSize = 14.sp, color = Primary, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)); features.forEach { f -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(f, fontSize = 13.sp, color = TextSecondary) }; Spacer(modifier = Modifier.height(4.dp)) }; Spacer(modifier = Modifier.height(8.dp)); Button(onClick = { /* TODO: Open payment/upgrade screen for $plan */ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) { Text("Upgrade to $plan", fontWeight = FontWeight.Bold) } } } }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

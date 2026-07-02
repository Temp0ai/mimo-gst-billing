package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayBookReportScreen(navController: NavController) {
    val today = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Day Book", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = BlueHeader)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Day Book", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(today, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }
            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("All transactions for today will be listed here.", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Features:", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("- Sale entries", fontSize = 13.sp, color = TextSecondary)
                    Text("- Purchase entries", fontSize = 13.sp, color = TextSecondary)
                    Text("- Expense entries", fontSize = 13.sp, color = TextSecondary)
                    Text("- Payment entries", fontSize = 13.sp, color = TextSecondary)
                    Text("- Income entries", fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}

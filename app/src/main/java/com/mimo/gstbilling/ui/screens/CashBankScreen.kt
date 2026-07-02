package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.CashBankViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBankScreen(navController: NavController, viewModel: CashBankViewModel = hiltViewModel()) {
    val transactions by viewModel.transactions.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cash & Bank", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Cash In", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", transactions.filter { it.type == "credit" }.sumOf { it.amount }), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenBalance) }
                        Column(horizontalAlignment = Alignment.End) { Text("Cash Out", fontSize = 12.sp, color = TextSecondary); Text(String.format(Locale.US, "\u20B9%,.2f", transactions.filter { it.type == "debit" }.sumOf { it.amount }), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent) }
                    }
                }
            }
            items(transactions) { txn ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { Text(txn.description, fontWeight = FontWeight.Bold, color = TextPrimary); Text(dateFormat.format(Date(txn.date)), fontSize = 12.sp, color = TextSecondary) }
                        Text(String.format(Locale.US, "\u20B9%,.2f", txn.amount), fontWeight = FontWeight.Bold, color = if (txn.type == "credit") GreenBalance else RedAccent)
                    }
                }
            }
            item { if (transactions.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No transactions yet", fontSize = 14.sp, color = TextSecondary) } } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

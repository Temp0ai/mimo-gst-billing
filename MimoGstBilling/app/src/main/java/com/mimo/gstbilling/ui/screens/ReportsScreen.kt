package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & GST", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            item { Text("GST Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ReportCard("GSTR-1 (Outward)", "Rs. 1,24,500", "5 invoices", Success) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReportCard("GSTR-3B Summary", "Rs. 45,200", "Monthly return", Primary) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Sales & Purchase", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ReportCard("Sales Report", "Rs. 1,24,500", "12 transactions", Success) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReportCard("Purchase Report", "Rs. 45,200", "8 transactions", Error) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Text("Party Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { ReportCard("Party Ledger", "Rs. 34,800", "3 active parties", Primary) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { ReportCard("Stock Report", "Rs. 2,50,000", "45 items", Warning) }
        }
    }
}

@Composable
private fun ReportCard(title: String, amount: String, subtitle: String, color: androidx.compose.ui.graphics.Color) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text(amount, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    val sections = listOf(
        "Sale Reports" to listOf(
            Triple("Sale Report", Icons.Filled.TrendingUp, Screen.Sales.route),
            Triple("Sale Return Report", Icons.Filled.TrendingDown, Screen.Sales.route),
            Triple("Party Wise Sale Report", Icons.Filled.Group, Screen.Parties.route),
            Triple("Item Wise Sale Report", Icons.Filled.Inventory, Screen.Items.route)
        ),
        "Purchase Reports" to listOf(
            Triple("Purchase Report", Icons.Filled.LocalShipping, Screen.Purchases.route),
            Triple("Purchase Return Report", Icons.Filled.TrendingDown, Screen.Purchases.route),
            Triple("Party Wise Purchase Report", Icons.Filled.Group, Screen.Parties.route),
            Triple("Item Wise Purchase Report", Icons.Filled.Inventory, Screen.Items.route)
        ),
        "Financial Reports" to listOf(
            Triple("Profit & Loss", Icons.Filled.PieChart, Screen.ProfitLossReport.route),
            Triple("Expense Report", Icons.Filled.Receipt, Screen.ExpenseCategoryReport.route),
            Triple("Day Book", Icons.Filled.Description, Screen.DayBookReport.route),
            Triple("Balance Sheet", Icons.Filled.AccountBalance, Screen.BalanceSheet.route),
            Triple("Cash Flow", Icons.Filled.TrendingUp, Screen.CashFlowReport.route)
        ),
        "Tax Reports (GST)" to listOf(
            Triple("GSTR-1 Report", Icons.Filled.Receipt, Screen.Gstr1Report.route),
            Triple("GSTR-3B Report", Icons.Filled.Description, Screen.Gstr3bReport.route),
            Triple("Tax Summary", Icons.Filled.Receipt, Screen.Gstr1Report.route)
        ),
        "Stock Reports" to listOf(
            Triple("Stock Summary", Icons.Filled.Inventory, Screen.Items.route),
            Triple("Stock Transfer", Icons.Filled.LocalShipping, Screen.StockTransfer.route),
            Triple("Low Stock Alert", Icons.Filled.Warning, Screen.Items.route)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            sections.forEach { (sectionTitle, reports) ->
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Text(sectionTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    }
                }
                items(reports) { (title, icon, route) ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp).clickable { if (route.isNotEmpty()) navController.navigate(route) }, shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.weight(1f))
                            Text(title, fontSize = 14.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

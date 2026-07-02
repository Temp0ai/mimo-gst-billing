package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.BlueHeader
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary

data class ReportItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    val saleReports = listOf(
        ReportItem("Sale Report", Icons.Filled.TrendingUp, Primary, Screen.Sales.route),
        ReportItem("Sale Return Report", Icons.Filled.TrendingDown, Color(0xFFFF9800), ""),
        ReportItem("Party Wise Sale Report", Icons.Filled.Group, Color(0xFF9C27B0), ""),
        ReportItem("Item Wise Sale Report", Icons.Filled.Inventory, Color(0xFF00BCD4), "")
    )
    val purchaseReports = listOf(
        ReportItem("Purchase Report", Icons.Filled.LocalShipping, RedAccent, Screen.Purchases.route),
        ReportItem("Purchase Return Report", Icons.Filled.TrendingDown, Color(0xFFFF9800), ""),
        ReportItem("Party Wise Purchase Report", Icons.Filled.Group, Color(0xFF9C27B0), ""),
        ReportItem("Item Wise Purchase Report", Icons.Filled.Inventory, Color(0xFF00BCD4), "")
    )
    val financialReports = listOf(
        ReportItem("Profit & Loss", Icons.Filled.PieChart, GreenBalance, Screen.ProfitLossReport.route),
        ReportItem("Expense Report", Icons.Filled.Receipt, Color(0xFFE91E63), Screen.ExpenseCategoryReport.route),
        ReportItem("Day Book", Icons.Filled.Description, BlueHeader, Screen.DayBookReport.route),
        ReportItem("Balance Sheet", Icons.Filled.AccountBalance, Color(0xFF795548), Screen.BalanceSheet.route),
        ReportItem("Cash Flow", Icons.Filled.TrendingUp, Color(0xFF00BCD4), Screen.CashFlowReport.route)
    )
    val taxReports = listOf(
        ReportItem("GSTR-1 Report", Icons.Filled.Receipt, Color(0xFF9C27B0), Screen.Gstr1Report.route),
        ReportItem("GSTR-3B Report", Icons.Filled.Description, Primary, Screen.Gstr3bReport.route),
        ReportItem("Tax Summary", Icons.Filled.Receipt, RedAccent, "")
    )
    val stockReports = listOf(
        ReportItem("Stock Summary", Icons.Filled.Inventory, Color(0xFF00BCD4), ""),
        ReportItem("Stock Transfer", Icons.Filled.LocalShipping, Primary, Screen.StockTransfer.route),
        ReportItem("Low Stock Alert", Icons.Filled.LocalOffer, RedAccent, "")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            item { ReportSectionHeader("Sale Reports") }
            items(saleReports) { report -> ReportRow(report) { if (report.route.isNotEmpty()) navController.navigate(report.route) } }

            item { ReportSectionHeader("Purchase Reports") }
            items(purchaseReports) { report -> ReportRow(report) { if (report.route.isNotEmpty()) navController.navigate(report.route) } }

            item { ReportSectionHeader("Financial Reports") }
            items(financialReports) { report -> ReportRow(report) { if (report.route.isNotEmpty()) navController.navigate(report.route) } }

            item { ReportSectionHeader("Tax Reports (GST)") }
            items(taxReports) { report -> ReportRow(report) { if (report.route.isNotEmpty()) navController.navigate(report.route) } }

            item { ReportSectionHeader("Stock Reports") }
            items(stockReports) { report -> ReportRow(report) { if (report.route.isNotEmpty()) navController.navigate(report.route) } }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ReportSectionHeader(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun ReportRow(report: ReportItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = report.icon,
                contentDescription = null,
                tint = report.color,
                modifier = Modifier.size(22.dp)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.title,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
    }
}

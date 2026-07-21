package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

data class ReportItem(val title: String, val route: String = "", val isPro: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    var showBanner by remember { mutableStateOf(true) }

    val sections = listOf(
        "Transaction" to listOf(
            ReportItem("Sale Report", Screen.Sales.route),
            ReportItem("Purchase Report", Screen.Purchases.route),
            ReportItem("Day Book", Screen.DayBookReport.route),
            ReportItem("All Transactions", Screen.AllTransactionsReport.route),
            ReportItem("Bill Wise Profit", Screen.BillWiseProfitLoss.route),
            ReportItem("Profit & Loss", Screen.ProfitLossReport.route),
            ReportItem("Cashflow", Screen.CashFlowReport.route),
            ReportItem("Balance Sheet", Screen.BalanceSheet.route)
        ),
        "Party reports" to listOf(
            ReportItem("Party Statement", Screen.Parties.route),
            ReportItem("Party Wise Profit & Loss", Screen.PartyWiseProfitLoss.route),
            ReportItem("All Parties Report", Screen.AllPartiesReport.route),
            ReportItem("Party Report by Items", Screen.PartyReportByItems.route),
            ReportItem("Sale/Purchase by Party", Screen.SalePurchaseByParty.route),
            ReportItem("Sale/Purchase By Party Groups", Screen.Parties.route)
        ),
        "GST reports" to listOf(
            ReportItem("GSTR-1", Screen.Gstr1Filing.route),
            ReportItem("GSTR-2", Screen.Gstr2Filing.route),
            ReportItem("GSTR-3B", Screen.Gstr3bFiling.route),
            ReportItem("GST Transaction report", Screen.GstrSummary.route),
            ReportItem("GSTR-9", Screen.Gstr9aFiling.route),
            ReportItem("Sale Summary by HSN", Screen.HsnSummary.route),
            ReportItem("SAC Report", Screen.SacReport.route),
            ReportItem("e-Way Bill", Screen.EWayBill.route)
        ),
        "Item/Stock reports" to listOf(
            ReportItem("Stock Summary Report", Screen.StockSummaryReport.route),
            ReportItem("Item Report by Party", Screen.ItemReportByParty.route),
            ReportItem("Item Wise Profit & Loss", Screen.ItemWiseProfitLoss.route),
            ReportItem("Low Stock Summary Report", Screen.LowStockReport.route),
            ReportItem("Item Detail Report", Screen.StockSummaryReport.route),
            ReportItem("Stock Detail Report", Screen.StockSummaryReport.route),
            ReportItem("Sale/Purchase By Item Category", Screen.CategorySalePurchase.route),
            ReportItem("Stock summary By Item Category", Screen.CategoryStockReport.route),
            ReportItem("Item Batch Report", Screen.ItemBatchTracking.route),
            ReportItem("Item Serial Report", Screen.SerialReport.route),
            ReportItem("Item Wise Discount", Screen.ItemWiseDiscount.route)
        ),
        "Business status" to listOf(
            ReportItem("Bank Statement", Screen.BankStatementReport.route),
            ReportItem("Discount Report", Screen.DiscountReport.route)
        ),
        "Taxes" to listOf(
            ReportItem("GST Report", Screen.GstrSummary.route),
            ReportItem("GST Rate Report", Screen.GstrSummary.route),
            ReportItem("Form No. 27EQ", Screen.Form27Eq.route),
            ReportItem("TCS Receivable", Screen.TcsReceivable.route),
            ReportItem("TDS Payable", Screen.TdsPayable.route),
            ReportItem("TDS Receivable", Screen.TdsReceivable.route)
        ),
        "Expense reports" to listOf(
            ReportItem("Expense Transaction Report", Screen.ExpenseTransactionReport.route),
            ReportItem("Expense Category Report", Screen.ExpenseCategoryReport.route),
            ReportItem("Expense Item Report", Screen.ExpenseItemReport.route)
        ),
        "Sale/Purchase Order reports" to listOf(
            ReportItem("Sale/Purchase Order Transaction Report", Screen.Orders.route),
            ReportItem("Sale/Purchase Order Item Report", Screen.OrderItemReport.route)
        ),
        "Loan Reports" to listOf(
            ReportItem("Loan Statement", Screen.LoanStatement.route)
        )
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
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
        ) {
            if (showBanner) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tap the \u2B50 icon to add a report to the top of this screen for easy access.",
                                fontSize = 13.sp,
                                color = Color(0xFF8D6E00),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showBanner = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF8D6E00), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            sections.forEach { (sectionTitle, reports) ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            Text(
                                sectionTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            )
                            reports.forEach { report ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (report.route.isNotEmpty()) navController.navigate(report.route)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        report.title,
                                        fontSize = 15.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (report.isPro) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEDE7F6), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Workspaces,
                                                contentDescription = "Pro",
                                                tint = Color(0xFF7E57C2),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFFBDBDBD),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (report != reports.last()) {
                                    HorizontalDivider(
                                        color = Color(0xFFF0F0F0),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

data class OnlineOrder(
    val id: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineOrderListScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Confirmed", "Shipped", "Delivered", "All")

    val orders = remember {
        listOf(
            OnlineOrder("ORD-001", "Rahul Sharma", 2499.0, "25 Jul 2026", "Pending"),
            OnlineOrder("ORD-002", "Priya Patel", 1599.0, "24 Jul 2026", "Confirmed"),
            OnlineOrder("ORD-003", "Amit Singh", 899.0, "24 Jul 2026", "Shipped"),
            OnlineOrder("ORD-004", "Neha Gupta", 3499.0, "23 Jul 2026", "Delivered"),
            OnlineOrder("ORD-005", "Vikram Kumar", 599.0, "23 Jul 2026", "Pending"),
            OnlineOrder("ORD-006", "Sonia Verma", 1299.0, "22 Jul 2026", "Confirmed"),
            OnlineOrder("ORD-007", "Rajesh Tiwari", 499.0, "22 Jul 2026", "Shipped"),
            OnlineOrder("ORD-008", "Anita Desai", 2199.0, "21 Jul 2026", "Delivered")
        )
    }

    val filteredOrders = when (selectedTab) {
        0 -> orders.filter { it.status == "Pending" }
        1 -> orders.filter { it.status == "Confirmed" }
        2 -> orders.filter { it.status == "Shipped" }
        3 -> orders.filter { it.status == "Delivered" }
        else -> orders
    }

    val statusColor = when {
        true -> VyaparStatusOrange
        else -> VyaparStatusGreen
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Online Orders", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Primary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Primary
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Primary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = VyaparEmptyStateIcon,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No ${tabs[selectedTab].lowercase()} orders", fontSize = 16.sp, color = VyaparEmptyStateText)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.clickable { navController.navigate(Screen.OrderDetail.createRoute(order.id.toLongOrNull() ?: 0L)) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.id, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                when (order.status) {
                                                    "Pending" -> VyaparWarningBackground
                                                    "Confirmed" -> VyaparInfoBackground
                                                    "Shipped" -> VyaparSuccessBackground
                                                    "Delivered" -> VyaparSuccessBackground
                                                    else -> VyaparUnselectedBg
                                                },
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            order.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = when (order.status) {
                                                "Pending" -> VyaparWarningText
                                                "Confirmed" -> VyaparInfoText
                                                "Shipped" -> VyaparSuccessText
                                                "Delivered" -> VyaparSuccessText
                                                else -> TextSecondary
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Customer", fontSize = 11.sp, color = TextSecondary)
                                        Text(order.customerName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                    Column {
                                        Text("Amount", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            "\u20B9${String.format("%,.0f", order.amount)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                    }
                                    Column {
                                        Text("Date", fontSize = 11.sp, color = TextSecondary)
                                        Text(order.date, fontSize = 13.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class AgingBucket(val label: String, val amount: Double, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgingReportScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val receivableAging = listOf(
        AgingBucket("Current (0 days)", 185000.0, GreenBalance),
        AgingBucket("1-30 days", 75000.0, Color(0xFFFFC107)),
        AgingBucket("31-60 days", 45000.0, Color(0xFFFF9800)),
        AgingBucket("61-90 days", 25000.0, RedAccent),
        AgingBucket("90+ days", 12000.0, Color(0xFFD32F2F))
    )

    val payableAging = listOf(
        AgingBucket("Current (0 days)", 92000.0, GreenBalance),
        AgingBucket("1-30 days", 35000.0, Color(0xFFFFC107)),
        AgingBucket("31-60 days", 18000.0, Color(0xFFFF9800)),
        AgingBucket("61-90 days", 8000.0, RedAccent),
        AgingBucket("90+ days", 3000.0, Color(0xFFD32F2F))
    )

    val data = if (selectedTab == 0) receivableAging else payableAging

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aging Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Receivable", "Payable").forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (index == 0) GreenBalance else RedAccent,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Total ${if (selectedTab == 0) "Receivable" else "Payable"}",
                        fontSize = 14.sp, color = TextSecondary
                    )
                    Text(
                        "\u20B9${String.format(java.util.Locale.US, "%,.2f", data.sumOf { it.amount })}",
                        fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) GreenBalance else RedAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data) { bucket ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(bucket.color, RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(bucket.label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Text("\u20B9${String.format(java.util.Locale.US, "%,.0f", bucket.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

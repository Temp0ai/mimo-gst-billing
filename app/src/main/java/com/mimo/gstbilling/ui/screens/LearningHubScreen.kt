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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class LearningItem(val id: String, val title: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningHubScreen(navController: NavController) {
    val items = listOf(
        LearningItem("1", "Creating Your First Invoice", "Learn how to create and send professional GST invoices", Icons.Filled.Receipt, "Getting Started"),
        LearningItem("2", "Managing Parties", "Add customers and suppliers, track balances", Icons.Filled.Group, "Getting Started"),
        LearningItem("3", "Inventory Management", "Track stock, set alerts, manage items", Icons.Filled.Inventory, "Getting Started"),
        LearningItem("4", "GST Filing Basics", "Understand GSTR-1, GSTR-3B filing process", Icons.Filled.Description, "GST"),
        LearningItem("5", "HSN/SAC Code Guide", "Find correct HSN codes for your products", Icons.Filled.Search, "GST"),
        LearningItem("6", "TDS/TCS Explained", "How to apply TDS and TCS in invoices", Icons.Filled.Calculate, "Advanced"),
        LearningItem("7", "Bank Reconciliation", "Match bank statements with transactions", Icons.Filled.AccountBalance, "Advanced"),
        LearningItem("8", "Barcode Scanning", "Use your phone camera to scan barcodes", Icons.Filled.QrCodeScanner, "Features"),
        LearningItem("9", "Multi-Store Management", "Manage inventory across multiple stores", Icons.Filled.Store, "Features"),
        LearningItem("10", "WhatsApp Integration", "Send invoices and reminders via WhatsApp", Icons.Filled.Chat, "Features"),
        LearningItem("11", "Custom Reports", "Build custom reports for your business needs", Icons.Filled.Assessment, "Reports"),
        LearningItem("12", "Data Backup & Restore", "Keep your data safe with regular backups", Icons.Filled.Backup, "Settings")
    )
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Getting Started", "GST", "Advanced", "Features", "Reports", "Settings")
    val filtered = if (selectedCategory == "All") items else items.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Learning Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            LazyRow(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category, fontSize = 12.sp) })
                }
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filtered) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(RedAccent.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(item.icon, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(item.description, fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Surface(shape = RoundedCornerShape(50), color = VyaparBlue.copy(alpha = 0.1f)) {
                                Text(item.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VyaparBlue)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

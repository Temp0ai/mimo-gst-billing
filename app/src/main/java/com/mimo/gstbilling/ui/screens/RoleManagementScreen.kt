package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
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

data class AppRole(val name: String, val permissions: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleManagementScreen(navController: NavController) {
    val roles = listOf(
        AppRole("Owner", listOf("All Access")),
        AppRole("Manager", listOf("View All", "Edit Invoices", "Manage Staff", "View Reports", "Manage Items")),
        AppRole("Accountant", listOf("View All", "Edit Invoices", "View Reports", "Manage Payments")),
        AppRole("Staff", listOf("View Items", "Create Invoices", "View Parties")),
        AppRole("Delivery Boy", listOf("View Orders", "Update Delivery Status"))
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Role Management", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            items(roles) { role ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) { Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))) { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Primary, modifier = Modifier.padding(8.dp).size(20.dp)) } }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(role.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        role.permissions.forEach { perm -> Text("\u2022 $perm", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(start = 52.dp, top = 2.dp)) }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

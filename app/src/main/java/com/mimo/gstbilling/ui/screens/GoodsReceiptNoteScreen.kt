package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
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
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsReceiptNoteScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Goods Receipt Note", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Inventory, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Goods Receipt Note", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Record goods received from suppliers against purchase orders.", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("GRN Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("GRN Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Supplier Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Purchase Order Reference") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Received By") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    }
                }
            }
            item { Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)) { Icon(Icons.Filled.CheckCircle, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Confirm Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp) } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

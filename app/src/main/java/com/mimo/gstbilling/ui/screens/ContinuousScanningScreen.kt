package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinuousScanningScreen(
    navController: NavController
) {
    var flashEnabled by remember { mutableStateOf(false) }
    var showQtyDialog by remember { mutableStateOf(false) }
    var qtyText by remember { mutableStateOf("1") }

    val scannedItems = remember {
        mutableStateListOf(
            Triple("Laptop HP 15s", "8901234567890", "1"),
            Triple("Mouse Logitech", "8901234567891", "2"),
            Triple("LED Bulb 9W", "8901234567892", "5")
        )
    }

    if (showQtyDialog) {
        AlertDialog(
            onDismissRequest = { showQtyDialog = false },
            title = { Text("Enter Quantity", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scannedItems.add(Triple("New Item", "8909999999999", qtyText))
                        showQtyDialog = false
                        qtyText = "1"
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showQtyDialog = false }) { Text("Cancel") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Continuous Scanning", fontWeight = FontWeight.Bold) },
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
                .background(LightBlueBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera Preview", color = Color.Gray, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(200.dp)
                        .border(2.dp, Primary, RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(2.dp)
                        .background(Color.Red)
                        .align(Alignment.Center)
                )
                FloatingActionButton(
                    onClick = { flashEnabled = !flashEnabled },
                    containerColor = if (flashEnabled) Color.Yellow else Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Flash",
                        tint = Color.Black
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recently Scanned (${scannedItems.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    scannedItems.forEach { (name, barcode, qty) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                Text(barcode, fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("Qty: $qty", fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Button(
                onClick = { showQtyDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Item Manually", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

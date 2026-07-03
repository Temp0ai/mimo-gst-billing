package com.mimo.gstbilling.ui.screens

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.LightBlueBg
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.utils.ThermalPrinter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalPrinterScreen(navController: NavController) {
    var isScanning by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var isConnected by remember { mutableStateOf(ThermalPrinter.isConnected()) }
    var printStatus by remember { mutableStateOf<String?>(null) }

    val bluetoothAdapter = remember { BluetoothAdapter.getDefaultAdapter() }

    fun scanDevices() {
        isScanning = true
        devices = try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
        isScanning = false
    }

    LaunchedEffect(Unit) { scanDevices() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thermal Printer", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = if (isConnected) GreenBalance else TextSecondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Printer Status", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(if (isConnected) "Connected" else "Not Connected", fontSize = 13.sp, color = if (isConnected) GreenBalance else RedAccent)
                            }
                            if (isConnected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenBalance)
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Paired Bluetooth Devices", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        } else if (devices.isEmpty()) {
                            Text("No paired devices found. Pair a printer in Android Settings > Bluetooth.", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }

            items(devices) { device ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedDevice?.address == device.address) Primary.copy(alpha = 0.1f) else Color.White),
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedDevice = device
                        ThermalPrinter.connect(device.address)
                        isConnected = ThermalPrinter.isConnected()
                    }
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Print, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(device.name ?: "Unknown Device", fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text(device.address, fontSize = 12.sp, color = TextSecondary)
                        }
                        if (selectedDevice?.address == device.address && isConnected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenBalance)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { scanDevices() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Refresh Devices")
                }
            }

            if (isConnected) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GreenBalance.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Printer Connected!", fontWeight = FontWeight.Bold, color = GreenBalance)
                            Text("You can now print invoices from the invoice detail screen.", fontSize = 13.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    ThermalPrinter.printText("Test Print from Mimo GST Billing\nDate: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(java.util.Date())}\n\n")
                                    printStatus = "Test page sent"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenBalance)
                            ) {
                                Text("Test Print", color = Color.White)
                            }
                            if (printStatus != null) {
                                Text(printStatus!!, fontSize = 12.sp, color = GreenBalance, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }

            if (!isConnected) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Connect a thermal printer to print invoices directly. Make sure the printer is paired via Bluetooth settings.", fontSize = 13.sp, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        }
    }
}

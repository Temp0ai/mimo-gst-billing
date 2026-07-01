package com.mimo.gstbilling.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    navController: NavController,
    onBarcodeScanned: (String) -> Unit = {}
) {
    var barcode by remember { mutableStateOf("") }
    var manualEntry by remember { mutableStateOf("") }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // In a real app, use ML Kit or ZXing to decode barcode from bitmap
        // For now, simulate with manual entry
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Launch camera
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Scanner", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(80.dp)
                    )
                    Text("Scan Barcode", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Point your camera at a barcode to scan", fontSize = 14.sp, color = TextSecondary)

                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                // Launch camera scanner
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.height(0.dp))
                        Text("  Open Camera Scanner", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("OR", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enter Barcode Manually", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    OutlinedTextField(
                        value = manualEntry,
                        onValueChange = { manualEntry = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type barcode number") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Button(
                        onClick = {
                            if (manualEntry.isNotBlank()) {
                                onBarcodeScanned(manualEntry)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBalance),
                        shape = RoundedCornerShape(8.dp),
                        enabled = manualEntry.isNotBlank()
                    ) {
                        Text("Search Item", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

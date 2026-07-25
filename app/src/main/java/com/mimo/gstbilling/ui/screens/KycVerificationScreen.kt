package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycVerificationScreen(
    navController: NavController
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var selectedDocType by remember { mutableStateOf("PAN") }
    var documentNumber by remember { mutableStateOf("") }
    var docStatus by remember { mutableStateOf("pending") }
    var expanded by remember { mutableStateOf(false) }
    val docTypes = listOf("PAN", "GSTIN", "Aadhaar", "Bank Account")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KYC Verification", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("PAN", "GSTIN", "Bank", "Verify").forEachIndexed { index, label ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentStep >= index) Primary else Primary.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentStep > index) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                } else {
                                    Text("${index + 1}", fontWeight = FontWeight.Bold, color = if (currentStep >= index) Color.White else Primary, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, fontSize = 11.sp, color = if (currentStep >= index) TextPrimary else TextSecondary)
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Select Document Type", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDocType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Document Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Divider
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            docTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedDocType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = documentNumber,
                        onValueChange = { documentNumber = it },
                        label = { Text("Document Number") },
                        placeholder = {
                            Text(
                                when (selectedDocType) {
                                    "PAN" -> "ABCDE1234F"
                                    "GSTIN" -> "27AABCU9603R1ZM"
                                    "Aadhaar" -> "1234 5678 9012"
                                    else -> "Account Number"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Divider
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = { /* Upload document */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Document")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when (docStatus) {
                                    "verified" -> GreenBalance.copy(alpha = 0.1f)
                                    "rejected" -> RedAccent.copy(alpha = 0.1f)
                                    else -> Warning.copy(alpha = 0.1f)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            when (docStatus) {
                                "verified" -> Icons.Filled.CheckCircle
                                "rejected" -> Icons.Filled.Error
                                else -> Icons.Filled.Schedule
                            },
                            contentDescription = null,
                            tint = when (docStatus) {
                                "verified" -> GreenBalance
                                "rejected" -> RedAccent
                                else -> Warning
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (docStatus) {
                                "verified" -> "Document Verified"
                                "rejected" -> "Document Rejected - Please re-upload"
                                else -> "Verification Pending"
                            },
                            color = when (docStatus) {
                                "verified" -> GreenBalance
                                "rejected" -> RedAccent
                                else -> Warning
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (currentStep < 3) currentStep++ else navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    if (currentStep < 3) "Next Step" else "Complete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

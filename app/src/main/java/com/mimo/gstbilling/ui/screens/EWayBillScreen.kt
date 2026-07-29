package com.mimo.gstbilling.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EWayBillStates = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
    "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
    "Uttar Pradesh", "Uttarakhand", "West Bengal",
    "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EWayBillScreen(
    navController: NavController,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var gstin by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isGstnLoggedIn by remember { mutableStateOf(false) }
    var isGstnSectionExpanded by remember { mutableStateOf(true) }
    var isOtpSent by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    var invoiceNumber by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var partyGstin by remember { mutableStateOf("") }
    var placeOfSupply by remember { mutableStateOf("") }
    var showPlaceDropdown by remember { mutableStateOf(false) }
    var invoiceValue by remember { mutableStateOf("") }
    var hsnCode by remember { mutableStateOf("") }
    var transporterName by remember { mutableStateOf("") }
    var transporterGstin by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedEwb by remember { mutableStateOf<GeneratedEwb?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-Way Bill", fontWeight = FontWeight.Bold) },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // GSTN Login Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(CardCorner.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGstnSectionExpanded = !isGstnSectionExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.VpnKey,
                            contentDescription = null,
                            tint = if (isGstnLoggedIn) Success else Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GSTN Login", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text(
                                if (isGstnLoggedIn) "Logged in to GSTN" else "Login to generate e-Way Bills",
                                fontSize = 13.sp,
                                color = if (isGstnLoggedIn) Success else TextSecondary
                            )
                        }
                        Icon(
                            if (isGstnSectionExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(
                        visible = isGstnSectionExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = gstin,
                                onValueChange = { gstin = it },
                                label = { Text("GSTIN") },
                                singleLine = true,
                                enabled = !isGstnLoggedIn,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    if (isGstnLoggedIn) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success)
                                    }
                                }
                            )

                            if (!isGstnLoggedIn) {
                                Button(
                                    onClick = {
                                        if (gstin.isNotBlank()) {
                                            isOtpSent = true
                                            scope.launch {
                                                snackbarHostState.showSnackbar("OTP sent to registered mobile")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(CardCorner.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    enabled = gstin.isNotBlank() && !isOtpSent
                                ) {
                                    Icon(Icons.Filled.Sms, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send OTP")
                                }

                                if (isOtpSent) {
                                    OutlinedTextField(
                                        value = otp,
                                        onValueChange = { otp = it },
                                        label = { Text("Enter OTP") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            isVerifying = true
                                            scope.launch {
                                                delay(1000)
                                                isVerifying = false
                                                isGstnLoggedIn = true
                                                isGstnSectionExpanded = false
                                                snackbarHostState.showSnackbar("Logged in to GSTN")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(CardCorner.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                                        enabled = otp.isNotBlank() && !isVerifying
                                    ) {
                                        if (isVerifying) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        } else {
                                            Icon(Icons.Filled.Login, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text("Verify & Login")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // E-Way Bill Form
            AnimatedVisibility(visible = isGstnLoggedIn) {
                Column {
                    // Invoice & Party Details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(CardCorner.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Invoice & Party Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = { Text("Invoice Number *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = partyName,
                                onValueChange = { partyName = it },
                                label = { Text("Party Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = partyGstin,
                                onValueChange = { partyGstin = it },
                                label = { Text("Party GSTIN") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box {
                                OutlinedTextField(
                                    value = placeOfSupply,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Place of Supply *") },
                                    trailingIcon = {
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showPlaceDropdown = true })
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = showPlaceDropdown,
                                    onDismissRequest = { showPlaceDropdown = false }
                                ) {
                                    EWayBillStates.forEach { state ->
                                        val itemText: @Composable () -> Unit = { Text(state) }
                                        DropdownMenuItem(
                                            text = itemText,
                                            onClick = {
                                                placeOfSupply = state
                                                showPlaceDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = invoiceValue,
                                onValueChange = { invoiceValue = it },
                                label = { Text("Invoice Value *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("Rs. ") }
                            )

                            OutlinedTextField(
                                value = hsnCode,
                                onValueChange = { hsnCode = it },
                                label = { Text("HSN Code *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Transporter Details
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(CardCorner.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Transporter Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                            OutlinedTextField(
                                value = transporterName,
                                onValueChange = { transporterName = it },
                                label = { Text("Transporter Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = transporterGstin,
                                onValueChange = { transporterGstin = it },
                                label = { Text("Transporter GSTIN") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = { Text("Vehicle Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = distance,
                                onValueChange = { distance = it },
                                label = { Text("Distance (km)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Generate Button
                    Button(
                        onClick = {
                            isGenerating = true
                            scope.launch {
                                delay(2000)
                                isGenerating = false
                                generatedEwb = GeneratedEwb(
                                    ewbNumber = "EWB${System.currentTimeMillis() % 1000000000}",
                                    generatedDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(java.util.Date()),
                                    validUntil = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(java.util.Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(CardCorner.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !isGenerating && invoiceNumber.isNotBlank() && partyName.isNotBlank() && placeOfSupply.isNotBlank() && invoiceValue.isNotBlank() && hsnCode.isNotBlank()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating e-Way Bill...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        } else {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate E-Way Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    // Generated E-Way Bill Card
                    if (generatedEwb != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(CardCorner.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "E-Way Bill Generated",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Success
                                )

                                HorizontalDivider(color = Divider)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    EwbDetailRow("EWB Number", generatedEwb!!.ewbNumber)
                                    EwbDetailRow("Generated Date", generatedEwb!!.generatedDate)
                                    EwbDetailRow("Valid Until", generatedEwb!!.validUntil)
                                    EwbDetailRow("Invoice Number", invoiceNumber)
                                    EwbDetailRow("Party", partyName)
                                    EwbDetailRow("Place of Supply", placeOfSupply)
                                    EwbDetailRow("Invoice Value", "Rs. $invoiceValue")
                                }

                                HorizontalDivider(color = Divider)

                                // QR Code Placeholder
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Background)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(24.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Filled.QrCode2,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("QR Code", fontSize = 12.sp, color = TextSecondary)
                                        Text(generatedEwb!!.ewbNumber, fontSize = 10.sp, color = TextSecondary)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("E-Way Bill downloaded")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(CardCorner.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                                    ) {
                                        Icon(Icons.Filled.Download, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Download")
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("E-Way Bill shared")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(CardCorner.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                    ) {
                                        Icon(Icons.Filled.Share, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Share")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Show message when not logged in
            if (!isGstnLoggedIn) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(CardCorner.dp),
                    colors = CardDefaults.cardColors(containerColor = LightBlueBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Login to GSTN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Enter your GSTIN and verify OTP to generate e-Way Bills directly from the app.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private data class GeneratedEwb(
    val ewbNumber: String,
    val generatedDate: String,
    val validUntil: String
)

@Composable
private fun EwbDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

package com.mimo.gstbilling.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.EWayBillEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.EWayBillViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val EWayBillStates = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
    "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
    "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi", "Chandigarh"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EWayBillScreen(navController: NavController, viewModel: EWayBillViewModel = hiltViewModel()) {
    val ewayBills by viewModel.ewayBills.collectAsState()
    val generatedEwb by viewModel.generatedEwb.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val context = LocalContext.current
    var showCreateForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-Way Bill", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateForm = !showCreateForm }, containerColor = VyaparBlue, contentColor = Color.White) {
                Icon(if (showCreateForm) Icons.Filled.Close else Icons.Filled.Add, contentDescription = if (showCreateForm) "Close" else "Generate E-Way Bill")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            if (showCreateForm || generatedEwb != null) {
                item { CreateEWayBillForm(viewModel, isGenerating, generatedEwb, onDismiss = { showCreateForm = false }) }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Generated E-Way Bills", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${ewayBills.size} total", fontSize = 13.sp, color = TextSecondary)
                }
            }

            if (ewayBills.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Description, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No E-Way Bills yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to generate an E-Way Bill", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(ewayBills, key = { it.id }) { ewb ->
                    EWayBillCard(ewb, viewModel)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CreateEWayBillForm(viewModel: EWayBillViewModel, isGenerating: Boolean, generatedEwb: EWayBillEntity?, onDismiss: () -> Unit) {
    var invoiceNumber by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var partyGstin by remember { mutableStateOf("") }
    var placeOfSupply by remember { mutableStateOf("") }
    var showPlaceDropdown by remember { mutableStateOf(false) }
    var invoiceValue by remember { mutableStateOf("") }
    var hsnCode by remember { mutableStateOf("") }
    var transporterName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var supplyType by remember { mutableStateOf("Outward") }
    var subSupplyType by remember { mutableStateOf("Supply") }

    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Generate E-Way Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary) }
            }

            OutlinedTextField(value = invoiceNumber, onValueChange = { invoiceNumber = it }, label = { Text("Invoice Number *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Receipt, contentDescription = null, tint = VyaparBlue) })
            OutlinedTextField(value = partyName, onValueChange = { partyName = it }, label = { Text("Party Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = VyaparBlue) })
            OutlinedTextField(value = partyGstin, onValueChange = { partyGstin = it }, label = { Text("Party GSTIN") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = VyaparBlue) })

            Box {
                OutlinedTextField(value = placeOfSupply, onValueChange = {}, readOnly = true, label = { Text("Place of Supply *") }, modifier = Modifier.fillMaxWidth().clickable { showPlaceDropdown = true }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }, leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = VyaparBlue) })
                DropdownMenu(expanded = showPlaceDropdown, onDismissRequest = { showPlaceDropdown = false }) {
                    EWayBillStates.forEach { state -> DropdownMenuItem(text = { Text(state) }, onClick = { placeOfSupply = state; showPlaceDropdown = false }) }
                }
            }

            OutlinedTextField(value = invoiceValue, onValueChange = { invoiceValue = it }, label = { Text("Invoice Value *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), leadingIcon = { Text("\u20B9", color = VyaparBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
            OutlinedTextField(value = hsnCode, onValueChange = { hsnCode = it }, label = { Text("HSN Code *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null, tint = VyaparBlue) })
            OutlinedTextField(value = transporterName, onValueChange = { transporterName = it }, label = { Text("Transporter Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = VyaparBlue) })
            OutlinedTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = { Text("Vehicle Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = VyaparBlue) })
            OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance (km)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), leadingIcon = { Icon(Icons.Filled.Straighten, contentDescription = null, tint = VyaparBlue) })

            Button(
                onClick = { viewModel.generateEWayBill(invoiceNumber, partyName, partyGstin.ifBlank { null }, placeOfSupply, invoiceValue.toDoubleOrNull() ?: 0.0, hsnCode, transporterName.ifBlank { null }, null, vehicleNumber.ifBlank { null }, distance.toIntOrNull(), supplyType, subSupplyType) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue),
                enabled = !isGenerating && invoiceNumber.isNotBlank() && partyName.isNotBlank() && placeOfSupply.isNotBlank() && invoiceValue.isNotBlank() && hsnCode.isNotBlank()
            ) {
                if (isGenerating) { CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                else { Icon(Icons.Filled.CheckCircle, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)) }
                Text("Generate E-Way Bill", fontWeight = FontWeight.Bold)
            }
        }
    }

    generatedEwb?.let { ewb ->
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = VyaparGreen, modifier = Modifier.size(48.dp))
                Text("E-Way Bill Generated!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VyaparGreen)
                HorizontalDivider(color = VyaparDivider)
                EwbDetailRow("EWB Number", ewb.ewbNumber)
                EwbDetailRow("Invoice", ewb.invoiceNumber)
                EwbDetailRow("Party", ewb.partyName)
                EwbDetailRow("Place of Supply", ewb.placeOfSupply)
                EwbDetailRow("Value", String.format(Locale.US, "\u20B9%,.2f", ewb.invoiceValue))
                EwbDetailRow("Valid Until", SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(ewb.validUntil)))

                if (ewb.qrCodeData != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = VyaparBackground)) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (File(ewb.qrCodeData).exists()) {
                                val bitmap = BitmapFactory.decodeFile(ewb.qrCodeData)
                                bitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(150.dp), contentScale = ContentScale.Fit) }
                            } else {
                                Icon(Icons.Filled.QrCode2, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(80.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Scan for E-Way Bill details", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EWayBillCard(ewb: EWayBillEntity, viewModel: EWayBillViewModel) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel E-Way Bill?", fontWeight = FontWeight.Bold) },
            text = { Text("Cancel EWB ${ewb.ewbNumber}? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.cancelEWayBill(ewb.id); showCancelDialog = false }) { Text("Cancel EWB", color = RedAccent, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showCancelDialog = false }) { Text("No") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(if (ewb.status == "ACTIVE") VyaparGreen.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = if (ewb.status == "ACTIVE") VyaparGreen else RedAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(ewb.ewbNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("${ewb.invoiceNumber} \u2022 ${ewb.partyName}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Surface(shape = RoundedCornerShape(50), color = if (ewb.status == "ACTIVE") VyaparGreen.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f)) {
                    Text(ewb.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (ewb.status == "ACTIVE") VyaparGreen else RedAccent)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            EwbDetailRow("Value", String.format(Locale.US, "\u20B9%,.2f", ewb.invoiceValue))
            EwbDetailRow("Place", ewb.placeOfSupply)
            EwbDetailRow("Generated", dateFormat.format(Date(ewb.generatedDate)))
            EwbDetailRow("Valid Until", dateFormat.format(Date(ewb.validUntil)))

            if (ewb.status == "ACTIVE") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val file = viewModel.generateEwbPdf(context, ewb)
                            if (file != null) {
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    setPackage("com.whatsapp")
                                    putExtra(Intent.EXTRA_TEXT, "E-Way Bill: ${ewb.ewbNumber}\nInvoice: ${ewb.invoiceNumber}\nParty: ${ewb.partyName}\nValue: ₹${String.format(Locale.US, "%,.2f", ewb.invoiceValue)}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share E-Way Bill"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val file = viewModel.generateEwbPdf(context, ewb)
                            if (file != null) {
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View PDF", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showCancelDialog = true }, colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) {
                        Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EwbDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

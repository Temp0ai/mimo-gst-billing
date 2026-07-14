package com.mimo.gstbilling.ui.screens

import android.graphics.pdf.PdfDocument
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EWayBillScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getInvoices().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormat = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US) }

    var fromState by remember { mutableStateOf("") }
    var toState by remember { mutableStateOf("") }
    var vehicleNo by remember { mutableStateOf("") }
    var transportMode by remember { mutableStateOf("Road") }
    var transporterName by remember { mutableStateOf("") }
    var transportDocNo by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("e-Way Bill", fontWeight = FontWeight.Bold) },
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Generate e-Way Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("e-Way bill is required for goods movement exceeding \u20B950,000", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Transport Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        OutlinedTextField(value = fromState, onValueChange = { fromState = it }, label = { Text("From State") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = toState, onValueChange = { toState = it }, label = { Text("To State") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = vehicleNo, onValueChange = { vehicleNo = it }, label = { Text("Vehicle Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance (km)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = transporterName, onValueChange = { transporterName = it }, label = { Text("Transporter Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = transportDocNo, onValueChange = { transportDocNo = it }, label = { Text("Transport Document No") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                        Text("Transport Mode", fontSize = 13.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Road", "Rail", "Air", "Ship").forEach { mode ->
                                FilterChip(
                                    selected = transportMode == mode,
                                    onClick = { transportMode = mode },
                                    label = { Text(mode, fontSize = 12.sp, color = if (transportMode == mode) Primary else TextSecondary) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary.copy(alpha = 0.12f),
                                        containerColor = Color.White,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = Color(0xFFE0E0E0),
                                        selectedBorderColor = Primary.copy(alpha = 0.4f),
                                        enabled = true,
                                        selected = transportMode == mode
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("Select Invoice for e-Way Bill", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            val saleInvoices = invoices.filter { it.invoiceType == "sales" && it.totalAmount > 50000 }
            if (saleInvoices.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No invoices above \u20B950,000", fontSize = 14.sp, color = TextSecondary)
                            Text("e-Way bill is required for invoices above \u20B950,000", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
            items(saleInvoices.size) { index ->
                val invoice = saleInvoices[index]
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Party #${invoice.partyId} \u2022 ${dateFormat.format(java.util.Date(invoice.invoiceDate))}", fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format(java.util.Locale.US, "\u20B9%,.2f", invoice.totalAmount), fontWeight = FontWeight.Bold, color = BlueHeader, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    val eWayBillJson = generateEWayBillJson(invoice, fromState, toState, vehicleNo, transportMode, transporterName, transportDocNo, distance)
                                    val fileName = "eWayBill_${invoice.invoiceNumber}.json"
                                    val file = java.io.File(context.cacheDir, fileName)
                                    file.writeText(eWayBillJson)
                                    PdfGenerator.sharePdf(context, file)
                                    scope.launch { kotlinx.coroutines.delay(100); file.delete() }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) { Text("Generate", fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

private fun generateEWayBillJson(
    invoice: com.mimo.gstbilling.data.local.entity.InvoiceEntity,
    fromState: String, toState: String, vehicleNo: String,
    transportMode: String, transporterName: String, transportDocNo: String, distance: String
): String {
    return """
{
  "supplyType": "O",
  "subSupplyType": "0",
  "docType": "INV",
  "docNo": "${invoice.invoiceNumber}",
  "docDate": "${java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(java.util.Date(invoice.invoiceDate))}",
  "fromGstin": "",
  "fromTrdName": "",
  "fromAddr1": "",
  "fromAddr2": "",
  "fromPlace": "",
  "fromPincode": 0,
  "fromStateCode": 0,
  "toGstin": "",
  "toTrdName": "Party ${invoice.partyId}",
  "toAddr1": "",
  "toAddr2": "",
  "toPlace": "",
  "toPincode": 0,
  "toStateCode": 0,
  "totalValue": ${invoice.subTotal},
  "cgstValue": ${invoice.cgstTotal},
  "sgstValue": ${invoice.sgstTotal},
  "igstValue": ${invoice.igstTotal},
  "cessValue": ${invoice.cessTotal},
  "totInvValue": ${invoice.totalAmount},
  "transporterId": "",
  "transporterName": "$transporterName",
  "transDocNo": "$transportDocNo",
  "transMode": "${transportMode.first()}",
  "distance": ${distance.toIntOrNull() ?: 0},
  "vehicleNo": "$vehicleNo",
  "vehicleType": "R"
}
    """.trimIndent()
}

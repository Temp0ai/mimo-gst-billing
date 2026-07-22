package com.mimo.gstbilling.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VyaparDataImportScreen(
    navController: NavController,
    importViewModel: ImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var importType by remember { mutableStateOf("") }
    var importStatus by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            isImporting = true
            try {
                when (importType) {
                    "parties" -> importPartiesFromCsv(context, it, importViewModel) { status ->
                        importStatus = status; isImporting = false
                    }
                    "items" -> importItemsFromCsv(context, it, importViewModel) { status ->
                        importStatus = status; isImporting = false
                    }
                    "invoices" -> importInvoicesFromCsv(context, it, importViewModel) { status ->
                        importStatus = status; isImporting = false
                    }
                }
            } catch (e: Exception) {
                importStatus = "Error: ${e.message}"; isImporting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from Vyapar", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
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
            // Header
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Import Data from Vyapar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Import your parties, items, and invoices from Vyapar CSV export files.", fontSize = 13.sp, color = TextSecondary)
                }
            }

            // How to export from Vyapar
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How to export from Vyapar:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF8D6E00))
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "1. Open Vyapar app on your phone",
                        "2. Go to Settings > Data Export",
                        "3. Select Parties / Items / Invoices",
                        "4. Choose CSV format and export",
                        "5. Transfer the CSV file to this phone",
                        "6. Use the buttons below to import"
                    ).forEach { Text(it, fontSize = 12.sp, color = Color(0xFF8D6E00)) }
                }
            }

            // CSV Format Guide
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Expected CSV Formats", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Parties CSV:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Primary)
                    Text("name,phone,email,gstin,address,state,stateCode,partyType,balance", fontSize = 11.sp, color = TextSecondary)
                    Text("Ravi Traders,9876543210,ravi@email.com,27AABCU9603R1ZM,Mumbai MH,Maharashtra,27,Customer,15000", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Items CSV:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Primary)
                    Text("name,hsnCode,description,salePrice,purchasePrice,gstRate,unit,stockQuantity,isService", fontSize = 11.sp, color = TextSecondary)
                    Text("Laptop,8471,HP Laptop 15,45000,38000,18,Pcs,10,false", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Invoices CSV:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Primary)
                    Text("partyName,invoiceNumber,invoiceDate,subTotal,discount,taxableAmount,cgst,sgst,igst,total,paid,status,type", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // Import Buttons
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Import Data", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    ImportButton("Import Parties", Icons.Filled.People, "Parties (Customers & Suppliers)", GreenBalance) {
                        importType = "parties"; csvLauncher.launch("text/*")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ImportButton("Import Items", Icons.Filled.Inventory, "Products & Services", Primary) {
                        importType = "items"; csvLauncher.launch("text/*")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ImportButton("Import Invoices", Icons.Filled.Receipt, "Sales & Purchase Invoices", Color(0xFFFF9800)) {
                        importType = "invoices"; csvLauncher.launch("text/*")
                    }
                }
            }

            // Google Sheets Import
            var showGoogleSheetsDialog by remember { mutableStateOf(false) }
            var googleSheetsUrl by remember { mutableStateOf("") }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Import from Google Sheets", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Paste your Google Sheets export URL or CSV data directly.", fontSize = 13.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showGoogleSheetsDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        enabled = !isImporting
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import from Google Sheets / Paste CSV")
                    }
                }
            }

            if (showGoogleSheetsDialog) {
                AlertDialog(
                    onDismissRequest = { showGoogleSheetsDialog = false },
                    title = { Text("Import Transaction Data", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Paste the Google Sheets export URL or the raw CSV text below:", fontSize = 13.sp, color = TextSecondary)
                            OutlinedTextField(
                                value = googleSheetsUrl,
                                onValueChange = { googleSheetsUrl = it },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                                label = { Text("URL or CSV Data") },
                                placeholder = { Text("https://docs.google.com/spreadsheets/d/.../export?format=csv\n\nOR paste the CSV text directly...") }
                            )
                            Text("Format: Date, Party Name, Phone, GSTIN, Order No, Invoice No, Transaction Type, Amount, Payment Type, Received, Balance", fontSize = 11.sp, color = Color.Gray)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showGoogleSheetsDialog = false
                                isImporting = true
                                importStatus = "Fetching data..."
                                val url = googleSheetsUrl.trim()
                                googleSheetsUrl = ""
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val csvData = if (url.startsWith("http")) {
                                            val connection = java.net.URL(url).openConnection()
                                            connection.connectTimeout = 15000
                                            connection.readTimeout = 15000
                                            connection.getInputStream().bufferedReader().readText()
                                        } else {
                                            url
                                        }
                                        importViewModel.importVyaparTransactions(csvData) { result ->
                                            importStatus = result; isImporting = false
                                        }
                                    } catch (e: Exception) {
                                        importStatus = "Error fetching URL: ${e.message}\nTip: Make sure the sheet is shared publicly or use 'Paste CSV' instead."; isImporting = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                        ) { Text("Import") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGoogleSheetsDialog = false; googleSheetsUrl = "" }) { Text("Cancel") }
                    }
                )
            }

            // Sample Data Generator
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Start with Sample Data", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Generate realistic Indian business data to explore the app.", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isImporting = true
                            importViewModel.generateSampleData { count ->
                                importStatus = "Generated $count sample records!"
                                isImporting = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        enabled = !isImporting
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Sample Data")
                        }
                    }
                }
            }

            // Status
            if (importStatus.isNotEmpty()) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (importStatus.startsWith("Error")) Color(0xFFFFEBEE) else Color(0xFFE8F5E9))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (importStatus.startsWith("Error")) Icons.Filled.Error else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (importStatus.startsWith("Error")) RedAccent else GreenBalance,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(importStatus, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { importStatus = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Filled.FileUpload, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}

private fun importPartiesFromCsv(context: Context, uri: Uri, viewModel: ImportViewModel, onResult: (String) -> Unit) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return onResult("Error: Cannot read file")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        reader.close()

        if (lines.size < 2) return onResult("Error: CSV file is empty or has no data rows")

        var count = 0
        lines.drop(1).forEach { line ->
            val parts = line.split(",").map { it.trim() }
            if (parts.size >= 9) {
                viewModel.addParty(
                    name = parts[0],
                    phone = parts[1].ifBlank { null },
                    email = parts[2].ifBlank { null },
                    gstin = parts[3].ifBlank { null },
                    address = parts[4].ifBlank { null },
                    state = parts[5].ifBlank { null },
                    stateCode = parts[6].ifBlank { null },
                    partyType = parts[7].ifBlank { "Customer" },
                    balance = parts[8].toDoubleOrNull() ?: 0.0
                )
                count++
            }
        }
        onResult("Successfully imported $count parties!")
    } catch (e: Exception) {
        onResult("Error: ${e.message}")
    }
}

private fun importItemsFromCsv(context: Context, uri: Uri, viewModel: ImportViewModel, onResult: (String) -> Unit) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return onResult("Error: Cannot read file")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        reader.close()

        if (lines.size < 2) return onResult("Error: CSV file is empty or has no data rows")

        var count = 0
        lines.drop(1).forEach { line ->
            val parts = line.split(",").map { it.trim() }
            if (parts.size >= 9) {
                viewModel.addItem(
                    name = parts[0],
                    hsnCode = parts[1].ifBlank { null },
                    description = parts[2].ifBlank { null },
                    salePrice = parts[3].toDoubleOrNull() ?: 0.0,
                    purchasePrice = parts[4].toDoubleOrNull() ?: 0.0,
                    gstRate = parts[5].toDoubleOrNull() ?: 18.0,
                    unit = parts[6].ifBlank { "Pcs" },
                    stockQuantity = parts[7].toDoubleOrNull() ?: 0.0,
                    isService = parts[8].lowercase() == "true"
                )
                count++
            }
        }
        onResult("Successfully imported $count items!")
    } catch (e: Exception) {
        onResult("Error: ${e.message}")
    }
}

private fun importInvoicesFromCsv(context: Context, uri: Uri, viewModel: ImportViewModel, onResult: (String) -> Unit) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return onResult("Error: Cannot read file")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val csvText = reader.readText()
        reader.close()

        val rows = parseFullCsv(csvText)
        if (rows.size < 5) return onResult("Error: Not enough data rows (${rows.size} found)")

        val headerIdx = rows.indexOfFirst { row ->
            row.any { it.contains("Date", ignoreCase = true) } && row.any { it.contains("Party Name", ignoreCase = true) }
        }
        if (headerIdx == -1) return onResult("Error: Could not find header row with 'Date' and 'Party Name' columns")

        val headers = rows[headerIdx].map { it.trim().lowercase() }
        val dateCol = headers.indexOfFirst { it == "date" }
        val partyCol = headers.indexOfFirst { it.contains("party name") || it == "party" }
        val phoneCol = headers.indexOfFirst { it.contains("phone") }
        val gstinCol = headers.indexOfFirst { it.contains("gstin") || it.contains("gst no") }
        val invNoCol = headers.indexOfFirst { it.contains("invoice no") || it.contains("ref no") }
        val typeCol = headers.indexOfFirst { it.contains("transaction type") }
        val amountCol = headers.indexOfFirst { it.contains("total amount") || it.contains("amount") }
        val receivedCol = headers.indexOfFirst { it.contains("received") }
        val balanceCol = headers.indexOfFirst { it.contains("balance") }
        val descCol = headers.indexOfFirst { it.contains("description") }

        if (partyCol == -1 || amountCol == -1) return onResult("Error: Missing required columns (Party Name or Amount)")

        var count = 0
        var skippedCancelled = 0
        val partyNamesAdded = mutableSetOf<String>()

        val dataRows = rows.drop(headerIdx + 1)
        for (cols in dataRows) {
            try {
                val partyName = cols.getOrElse(partyCol) { "" }.trim().replace("\n", " ")
                if (partyName.isBlank()) continue

                val txnType = cols.getOrElse(typeCol) { "" }.trim()
                if (txnType.contains("Cancelled", ignoreCase = true)) { skippedCancelled++; continue }
                if (txnType.contains("Estimate", ignoreCase = true) || txnType.contains("Quotation", ignoreCase = true)) continue

                val dateStr = cols.getOrElse(dateCol) { "" }.trim()
                val phone = cols.getOrElse(phoneCol) { "" }.trim().replace("+91", "").replace(" ", "").ifBlank { null }
                val gstin = cols.getOrElse(gstinCol) { "" }.trim().ifBlank { null }
                val invoiceNo = cols.getOrElse(invNoCol) { "" }.trim().ifBlank { "N/A" }
                val totalAmountStr = cols.getOrElse(amountCol) { "0" }.trim().replace(",", "")
                val receivedStr = cols.getOrElse(receivedCol) { "0" }.trim().replace(",", "")
                val description = cols.getOrElse(descCol) { "" }.trim().ifBlank { null }

                val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
                if (totalAmount <= 0) continue

                // Add party if new
                if (partyName !in partyNamesAdded) {
                    partyNamesAdded.add(partyName)
                    viewModel.addParty(
                        name = partyName, phone = phone, email = null, gstin = gstin,
                        address = null, state = gstin?.take(2), stateCode = gstin?.take(2),
                        partyType = "customer", balance = 0.0
                    )
                }

                val received = receivedStr.toDoubleOrNull() ?: 0.0
                val isPaid = received > 0 && totalAmount <= received + 0.01
                val isPartial = received > 0 && !isPaid
                val paymentStatus = when {
                    txnType.contains("Payment-in", ignoreCase = true) -> "paid"
                    isPaid -> "paid"
                    isPartial -> "partial"
                    else -> "unpaid"
                }

                // Parse date to yyyy-MM-dd
                val dateFormatted = try {
                    val parts = dateStr.split("/")
                    if (parts.size == 3) "${parts[2]}-${parts[1].padStart(2,'0')}-${parts[0].padStart(2,'0')}" else "2024-01-01"
                } catch (_: Exception) { "2024-01-01" }

                viewModel.addInvoice(
                    partyName = partyName, invoiceNumber = invoiceNo, invoiceDate = dateFormatted,
                    subTotal = totalAmount, discount = 0.0, taxableAmount = totalAmount,
                    cgst = 0.0, sgst = 0.0, igst = 0.0, total = totalAmount,
                    paid = received, status = paymentStatus, type = "sales"
                )
                count++
            } catch (_: Exception) { }
        }
        onResult("Imported $count invoices! ($skippedCancelled cancelled skipped)")
    } catch (e: Exception) {
        onResult("Error: ${e.message}")
    }
}

package com.mimo.gstbilling.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.entity.ItemEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val partyDao: PartyDao,
    private val itemDao: ItemDao
) : ViewModel() {
    suspend fun insertParties(parties: List<PartyEntity>): Int {
        var count = 0
        parties.forEach { party ->
            try { partyDao.insertParty(party); count++ } catch (_: Exception) { }
        }
        return count
    }

    suspend fun insertItems(items: List<ItemEntity>): Int {
        var count = 0
        items.forEach { item ->
            try { itemDao.insertItem(item); count++ } catch (_: Exception) { }
        }
        return count
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(
    navController: NavController,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importedCount by remember { mutableIntStateOf(0) }
    var failedCount by remember { mutableIntStateOf(0) }
    var importType by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    var showResult by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            isLoading = true
            statusMessage = "Reading CSV..."
            scope.launch {
                try {
                    val resolver = context.contentResolver
                    val inputStream = resolver.openInputStream(it)
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                    val lines = reader.readLines()
                    reader.close()

                    if (lines.size <= 1) {
                        errors = listOf("File is empty or has no data rows"); showResult = true; isLoading = false; return@launch
                    }

                    val header = lines[0].lowercase().split(",").map { h -> h.trim().removeSurrounding("\"") }

                    if (importType.contains("item", ignoreCase = true)) {
                        val result = parseItemsCsv(header, lines)
                        importedCount = viewModel.insertItems(result.first)
                        failedCount = result.first.size - importedCount
                        errors = result.second
                    } else {
                        val result = parsePartiesCsv(header, lines)
                        importedCount = viewModel.insertParties(result.first)
                        failedCount = result.first.size - importedCount
                        errors = result.second
                    }
                    showResult = true
                } catch (e: Exception) {
                    errors = listOf("Error reading file: ${e.message}"); showResult = true
                }
                isLoading = false
            }
        }
    }

    val xlsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            isLoading = true
            statusMessage = "Reading Vyapar Excel file..."
            scope.launch {
                try {
                    val resolver = context.contentResolver
                    val inputStream = resolver.openInputStream(it)

                    val wb = org.apache.poi.hssf.usermodel.HSSFWorkbook(inputStream)
                    val sheet = wb.getSheetAt(0)

                    if (sheet.physicalNumberOfRows <= 1) {
                        errors = listOf("Excel file has no data rows"); showResult = true; isLoading = false; wb.close(); return@launch
                    }

                    val headerRow = sheet.getRow(0)
                    val headers = (0 until headerRow.lastCellNum).map { i ->
                        headerRow.getCell(i)?.toString()?.trim()?.lowercase() ?: ""
                    }

                    if (importType.contains("item", ignoreCase = true)) {
                        val items = mutableListOf<ItemEntity>()
                        val errs = mutableListOf<String>()
                        for (i in 1..sheet.lastRowNum) {
                            try {
                                val row = sheet.getRow(i) ?: continue
                                val cols = (0 until headerRow.lastCellNum).map { j -> row.getCell(j)?.toString()?.trim() ?: "" }

                                val nameIdx = headers.indexOfFirst { it.contains("item name") || it.contains("name") || it.contains("item") }
                                if (nameIdx == -1) { continue }
                                val name = cols.getOrElse(nameIdx) { "" }
                                if (name.isBlank()) { errs.add("Row ${i + 1}: empty name"); continue }

                                val priceIdx = headers.indexOfFirst { it.contains("sale price") || it.contains("selling price") || it.contains("price") || it.contains("rate") }
                                val purchasePriceIdx = headers.indexOfFirst { it.contains("purchase price") || it.contains("cost price") || it.contains("buying") }
                                val hsnIdx = headers.indexOfFirst { it.contains("hsn") || it.contains("sac") }
                                val gstIdx = headers.indexOfFirst { it.contains("tax") || it.contains("gst") }
                                val unitIdx = headers.indexOfFirst { it.contains("unit") || it.contains("uom") }
                                val stockIdx = headers.indexOfFirst { it.contains("stock") || it.contains("quantity") || it.contains("opening") }
                                val codeIdx = headers.indexOfFirst { it.contains("item code") || it.contains("code") || it.contains("sku") }
                                val descIdx = headers.indexOfFirst { it.contains("description") || it.contains("desc") }

                                items.add(
                                    ItemEntity(
                                        companyId = 1L,
                                        name = name,
                                        hsnCode = hsnIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        description = descIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        salePrice = priceIdx.takeIf { it >= 0 }?.let { cols[it] }?.toDoubleOrNull() ?: 0.0,
                                        purchasePrice = purchasePriceIdx.takeIf { it >= 0 }?.let { cols[it] }?.toDoubleOrNull() ?: 0.0,
                                        gstRate = gstIdx.takeIf { it >= 0 }?.let { cols[it] }?.toDoubleOrNull() ?: 0.0,
                                        unit = unitIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null } ?: "NOS",
                                        stockQuantity = stockIdx.takeIf { it >= 0 }?.let { cols[it] }?.toDoubleOrNull() ?: 0.0,
                                        isService = false
                                    )
                                )
                            } catch (e: Exception) { errs.add("Row ${i + 1}: ${e.message}") }
                        }
                        importedCount = viewModel.insertItems(items)
                        failedCount = items.size - importedCount
                        errors = errs
                    } else {
                        val parties = mutableListOf<PartyEntity>()
                        val errs = mutableListOf<String>()
                        for (i in 1..sheet.lastRowNum) {
                            try {
                                val row = sheet.getRow(i) ?: continue
                                val cols = (0 until headerRow.lastCellNum).map { j -> row.getCell(j)?.toString()?.trim() ?: "" }

                                val nameIdx = headers.indexOfFirst { it.contains("party name") || it.contains("name") || it.contains("customer") || it.contains("supplier") || it.contains("company") }
                                if (nameIdx == -1) { continue }
                                val name = cols.getOrElse(nameIdx) { "" }
                                if (name.isBlank()) { errs.add("Row ${i + 1}: empty name"); continue }

                                val phoneIdx = headers.indexOfFirst { it.contains("phone") || it.contains("mobile") || it.contains("contact") }
                                val gstinIdx = headers.indexOfFirst { it.contains("gstin") || it.contains("gst") }
                                val emailIdx = headers.indexOfFirst { it.contains("email") || it.contains("mail") }
                                val addrIdx = headers.indexOfFirst { it.contains("address") || it.contains("addr") }
                                val typeIdx = headers.indexOfFirst { it.contains("type") || it.contains("party type") || it.contains("category") }
                                val stateIdx = headers.indexOfFirst { it.contains("state") }
                                val balanceIdx = headers.indexOfFirst { it.contains("balance") || it.contains("opening") }
                                val cityIdx = headers.indexOfFirst { it.contains("city") }
                                val pincodeIdx = headers.indexOfFirst { it.contains("pincode") || it.contains("pin") || it.contains("zip") }

                                val type = typeIdx.takeIf { it >= 0 }?.let { cols[it] }?.lowercase() ?: "customer"
                                val partyType = when {
                                    type.contains("supplier") || type.contains("vendor") -> "supplier"
                                    type.contains("both") -> "both"
                                    else -> "customer"
                                }

                                val address = buildString {
                                    addrIdx.takeIf { it >= 0 }?.let { cols[it] }?.let { append(it) }
                                    cityIdx.takeIf { it >= 0 }?.let { cols[it] }?.let { if (isNotEmpty()) append(", "); append(it) }
                                    pincodeIdx.takeIf { it >= 0 }?.let { cols[it] }?.let { if (isNotEmpty()) append(" - "); append(it) }
                                }.ifBlank { null }

                                parties.add(
                                    PartyEntity(
                                        companyId = 1L,
                                        name = name,
                                        phone = phoneIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        gstin = gstinIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        email = emailIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        address = address,
                                        state = stateIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        stateCode = null,
                                        balance = balanceIdx.takeIf { it >= 0 }?.let { cols[it] }?.toDoubleOrNull() ?: 0.0,
                                        partyType = partyType
                                    )
                                )
                            } catch (e: Exception) { errs.add("Row ${i + 1}: ${e.message}") }
                        }
                        importedCount = viewModel.insertParties(parties)
                        failedCount = parties.size - importedCount
                        errors = errs
                    }
                    showResult = true
                    wb.close()
                } catch (e: Exception) {
                    errors = listOf("Error reading Excel file: ${e.message}"); showResult = true
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null, tint = Primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Import Data", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Import from Vyapar Excel, CSV, or any spreadsheet", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.navigate(Screen.VyaparImport.route) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) {
                        Icon(Icons.Filled.Storage, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Import Vyapar Backup (.vyb)")
                    }
                }
            }

            // Vyapar Excel Import
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFF1B5E20).copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.TableChart, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Import from Vyapar (.xls)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Direct import Vyapar Excel export files", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Parties from Vyapar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Columns auto-detected: Party Name, Phone, GSTIN, Email, Address, State, Type, Balance", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { importType = "vyapar_parties"; xlsLauncher.launch(arrayOf("application/vnd.ms-excel", "*/*")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) {
                        Text("Import Vyapar Parties (.xls)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Items from Vyapar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Columns auto-detected: Item Name, Code, Sale Price, Purchase Price, HSN, Tax Rate, Stock, Unit", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = { importType = "vyapar_items"; xlsLauncher.launch(arrayOf("application/vnd.ms-excel", "*/*")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))) {
                        Text("Import Vyapar Items (.xls)")
                    }
                }
            }

            // CSV Import
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Import from CSV", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Generic CSV import for any billing app", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(onClick = { importType = "parties"; csvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "*/*")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.People, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Import Parties CSV")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { importType = "items"; csvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "*/*")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))) {
                        Icon(Icons.Filled.Inventory, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Import Items CSV")
                    }
                }
            }

            if (isLoading) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(statusMessage.ifBlank { "Importing data..." }, color = TextPrimary)
                    }
                }
            }

            if (showResult) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (importedCount > 0) GreenBalance.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (importedCount > 0) Icons.Filled.CheckCircle else Icons.Filled.Error, contentDescription = null, tint = if (importedCount > 0) GreenBalance else RedAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (importedCount > 0) "Imported $importedCount records" else "Import failed", fontWeight = FontWeight.Bold, color = if (importedCount > 0) GreenBalance else RedAccent)
                        }
                        if (failedCount > 0) Text("$failedCount rows failed", fontSize = 12.sp, color = RedAccent)
                        if (errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Errors:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            errors.take(5).forEach { Text(it, fontSize = 11.sp, color = TextSecondary) }
                            if (errors.size > 5) Text("... and ${errors.size - 5} more errors", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

private fun parseItemsCsv(header: List<String>, lines: List<String>): Pair<List<ItemEntity>, List<String>> {
    val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("item") }
    val priceIdx = header.indexOfFirst { it.contains("price") || it.contains("rate") || it.contains("selling") }
    val purchasePriceIdx = header.indexOfFirst { it.contains("purchase price") || it.contains("cost") }
    val hsnIdx = header.indexOfFirst { it.contains("hsn") }
    val gstIdx = header.indexOfFirst { it.contains("gst") || it.contains("tax") }
    val unitIdx = header.indexOfFirst { it.contains("unit") || it.contains("qty") }
    val stockIdx = header.indexOfFirst { it.contains("stock") || it.contains("quantity") || it.contains("opening") }
    val descIdx = header.indexOfFirst { it.contains("description") || it.contains("desc") }

    if (nameIdx == -1) return Pair(emptyList(), listOf("Could not find 'name' column in header: ${lines[0]}"))

    val items = mutableListOf<ItemEntity>()
    val errs = mutableListOf<String>()
    for (i in 1 until lines.size) {
        try {
            val cols = parseCsvLine(lines[i])
            val name = cols.getOrElse(nameIdx) { "" }.trim()
            if (name.isBlank()) { errs.add("Row ${i + 1}: empty name"); continue }
            items.add(
                ItemEntity(
                    companyId = 1L, name = name,
                    hsnCode = hsnIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                    description = descIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                    salePrice = priceIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                    purchasePrice = purchasePriceIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                    gstRate = gstIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                    unit = unitIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "NOS" }?.trim() } ?: "NOS",
                    stockQuantity = stockIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                    isService = false
                )
            )
        } catch (e: Exception) { errs.add("Row ${i + 1}: ${e.message}") }
    }
    return Pair(items, errs)
}

private fun parsePartiesCsv(header: List<String>, lines: List<String>): Pair<List<PartyEntity>, List<String>> {
    val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("party") || it.contains("customer") || it.contains("company") }
    val phoneIdx = header.indexOfFirst { it.contains("phone") || it.contains("mobile") || it.contains("contact") }
    val gstinIdx = header.indexOfFirst { it.contains("gstin") || it.contains("gst") || it.contains("tax") }
    val emailIdx = header.indexOfFirst { it.contains("email") || it.contains("mail") }
    val addrIdx = header.indexOfFirst { it.contains("address") || it.contains("addr") || it.contains("city") }
    val typeIdx = header.indexOfFirst { it.contains("type") || it.contains("party type") || it.contains("category") }
    val stateIdx = header.indexOfFirst { it.contains("state") }
    val balanceIdx = header.indexOfFirst { it.contains("balance") || it.contains("opening") }

    if (nameIdx == -1) return Pair(emptyList(), listOf("Could not find 'name' column in header: ${lines[0]}"))

    val parties = mutableListOf<PartyEntity>()
    val errs = mutableListOf<String>()
    for (i in 1 until lines.size) {
        try {
            val cols = parseCsvLine(lines[i])
            val name = cols.getOrElse(nameIdx) { "" }.trim()
            if (name.isBlank()) { errs.add("Row ${i + 1}: empty name"); continue }
            val type = typeIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim()?.lowercase() } ?: "customer"
            val partyType = when {
                type.contains("supplier") || type.contains("vendor") -> "supplier"
                type.contains("both") -> "both"
                else -> "customer"
            }
            parties.add(
                PartyEntity(
                    companyId = 1L, name = name,
                    phone = phoneIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                    gstin = gstinIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                    email = emailIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                    address = addrIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                    state = stateIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                    stateCode = null,
                    balance = balanceIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                    partyType = partyType
                )
            )
        } catch (e: Exception) { errs.add("Row ${i + 1}: ${e.message}") }
    }
    return Pair(parties, errs)
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false
    for (c in line) {
        when {
            c == '"' -> inQuotes = !inQuotes
            c == ',' && !inQuotes -> { result.add(current.toString()); current = StringBuilder() }
            else -> current.append(c)
        }
    }
    result.add(current.toString())
    return result
}

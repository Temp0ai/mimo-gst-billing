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
            try {
                partyDao.insertParty(party)
                count++
            } catch (_: Exception) { }
        }
        return count
    }

    suspend fun insertItems(items: List<ItemEntity>): Int {
        var count = 0
        items.forEach { item ->
            try {
                itemDao.insertItem(item)
                count++
            } catch (_: Exception) { }
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

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            isLoading = true
            scope.launch {
                try {
                    val resolver = context.contentResolver
                    val inputStream = resolver.openInputStream(it)
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                    val lines = reader.readLines()
                    reader.close()

                    if (lines.size <= 1) {
                        errors = listOf("File is empty or has no data rows")
                        showResult = true
                        isLoading = false
                        return@launch
                    }

                    val header = lines[0].lowercase().split(",").map { h -> h.trim().removeSurrounding("\"") }

                    if (importType.contains("item", ignoreCase = true)) {
                        val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("item") }
                        val priceIdx = header.indexOfFirst { it.contains("price") || it.contains("rate") || it.contains("selling") }
                        val hsnIdx = header.indexOfFirst { it.contains("hsn") }
                        val gstIdx = header.indexOfFirst { it.contains("gst") || it.contains("tax") }
                        val unitIdx = header.indexOfFirst { it.contains("unit") || it.contains("qty") }
                        val catIdx = header.indexOfFirst { it.contains("category") || it.contains("group") }
                        val stockIdx = header.indexOfFirst { it.contains("stock") || it.contains("quantity") || it.contains("opening") }

                        if (nameIdx == -1) {
                            errors = listOf("Could not find 'name' column in header: ${lines[0]}")
                            showResult = true
                            isLoading = false
                            return@launch
                        }

                        val items = mutableListOf<ItemEntity>()
                        val errs = mutableListOf<String>()
                        for (i in 1 until lines.size) {
                            try {
                                val cols = parseCsvLine(lines[i])
                                val name = cols.getOrElse(nameIdx) { "" }.trim()
                                if (name.isBlank()) { errs.add("Row ${i + 1}: empty name"); continue }
                                items.add(
                                    ItemEntity(
                                        companyId = 1L,
                                        name = name,
                                        hsnCode = hsnIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() },
                                        description = null,
                                        salePrice = priceIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                                        purchasePrice = 0.0,
                                        gstRate = gstIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                                        unit = unitIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "NOS" }?.trim() } ?: "NOS",
                                        stockQuantity = stockIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
                                        isService = false
                                    )
                                )
                            } catch (e: Exception) { errs.add("Row ${i + 1}: ${e.message}") }
                        }
                        importedCount = viewModel.insertItems(items)
                        failedCount = items.size - importedCount
                        errors = errs
                    } else {
                        val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("party") || it.contains("customer") || it.contains("company") }
                        val phoneIdx = header.indexOfFirst { it.contains("phone") || it.contains("mobile") || it.contains("contact") }
                        val gstinIdx = header.indexOfFirst { it.contains("gstin") || it.contains("gst") || it.contains("tax") }
                        val emailIdx = header.indexOfFirst { it.contains("email") || it.contains("mail") }
                        val addrIdx = header.indexOfFirst { it.contains("address") || it.contains("addr") || it.contains("city") }
                        val typeIdx = header.indexOfFirst { it.contains("type") || it.contains("party type") || it.contains("category") }
                        val stateIdx = header.indexOfFirst { it.contains("state") }
                        val balanceIdx = header.indexOfFirst { it.contains("balance") || it.contains("opening") }

                        if (nameIdx == -1) {
                            errors = listOf("Could not find 'name' column in header: ${lines[0]}")
                            showResult = true
                            isLoading = false
                            return@launch
                        }

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
                                        companyId = 1L,
                                        name = name,
                                        phone = phoneIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                                        gstin = gstinIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                                        email = emailIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                                        address = addrIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                                        state = stateIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "" }?.trim() }?.ifBlank { null },
                                        balance = balanceIdx.takeIf { it >= 0 }?.let { cols.getOrElse(it) { "0" }?.trim()?.toDoubleOrNull() } ?: 0.0,
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
                } catch (e: Exception) {
                    errors = listOf("Error reading file: ${e.message}")
                    showResult = true
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data", fontWeight = FontWeight.Bold) },
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
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null, tint = Primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Import from CSV", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Import data from Vyapar, Tally, or any CSV file", fontSize = 13.sp, color = TextSecondary)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.People, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Import Parties", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Customers, suppliers from CSV", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Supported columns: name, phone, gstin, email, address, state, type, balance", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            importType = "parties"
                            launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Select Parties CSV") }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, tint = Color(0xFF00BCD4), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Import Items", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Products, services, HSN codes from CSV", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Supported columns: name, hsn, price, gst, unit, category, stock", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            importType = "items"
                            launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
                    ) { Text("Select Items CSV") }
                }
            }

            if (isLoading) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Importing data...", color = TextPrimary)
                    }
                }
            }

            if (showResult) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (importedCount > 0) GreenBalance.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (importedCount > 0) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = if (importedCount > 0) GreenBalance else RedAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (importedCount > 0) "Imported $importedCount $importType records" else "Import failed",
                                fontWeight = FontWeight.Bold,
                                color = if (importedCount > 0) GreenBalance else RedAccent
                            )
                        }
                        if (failedCount > 0) Text("$failedCount rows failed", fontSize = 12.sp, color = RedAccent)
                        if (errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Errors:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            errors.take(5).forEach { err ->
                                Text(err, fontSize = 11.sp, color = TextSecondary)
                            }
                            if (errors.size > 5) {
                                Text("... and ${errors.size - 5} more errors", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
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

package com.mimo.gstbilling.ui.screens

import android.net.Uri
import android.database.sqlite.SQLiteDatabase
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
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.ItemEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import com.mimo.gstbilling.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VyaparImportScreen(
    navController: NavController,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importedParties by remember { mutableIntStateOf(0) }
    var importedItems by remember { mutableIntStateOf(0) }
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    var tablesFound by remember { mutableStateOf<List<String>>(emptyList()) }
    var showResult by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            isLoading = true
            statusMessage = "Reading Vyapar database..."
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val resolver = context.contentResolver
                        val inputStream = resolver.openInputStream(it) ?: throw Exception("Cannot open file")
                        val tempFile = File(context.cacheDir, "vyapar_import.db")
                        tempFile.outputStream().use { out -> inputStream.copyTo(out) }
                        inputStream.close()

                        val db = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
                        val allTables = mutableListOf<String>()
                        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                        while (cursor.moveToNext()) {
                            allTables.add(cursor.getString(0))
                        }
                        cursor.close()
                        tablesFound = allTables

                        val errs = mutableListOf<String>()

                        // Try to find and import parties
                        val partyTable = allTables.find { t ->
                            t.contains("party", ignoreCase = true) || t.contains("contact", ignoreCase = true) || t.contains("customer", ignoreCase = true) || t.contains("supplier", ignoreCase = true)
                        }
                        if (partyTable != null) {
                            statusMessage = "Importing parties from $partyTable..."
                            val result = importPartiesFromVyaparDb(db, partyTable, viewModel)
                            importedParties = result.first
                            errs.addAll(result.second)
                        }

                        // Try to find and import items
                        val itemTable = allTables.find { t ->
                            t.contains("item", ignoreCase = true) || t.contains("product", ignoreCase = true) || t.contains("inventory", ignoreCase = true)
                        }
                        if (itemTable != null) {
                            statusMessage = "Importing items from $itemTable..."
                            val result = importItemsFromVyaparDb(db, itemTable, viewModel)
                            importedItems = result.first
                            errs.addAll(result.second)
                        }

                        if (partyTable == null && itemTable == null) {
                            errs.add("No party or item tables found. Tables: ${allTables.joinToString(", ")}")
                        }

                        errors = errs
                        db.close()
                        tempFile.delete()
                    }
                    showResult = true
                } catch (e: Exception) {
                    errors = listOf("Error: ${e.message}")
                    showResult = true
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from Vyapar", fontWeight = FontWeight.Bold) },
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
                    Box(modifier = Modifier.size(56.dp).background(Color(0xFF1B5E20).copy(alpha = 0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Import Vyapar Backup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Import parties, items from Vyapar .vyb backup files", fontSize = 13.sp, color = TextSecondary)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("How to export from Vyapar:", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "1. Open Vyapar app",
                        "2. Go to Settings > Backup & Restore",
                        "3. Tap 'Backup to phone'",
                        "4. Save the .vyb file",
                        "5. Transfer file to this phone",
                        "6. Tap button below to import"
                    ).forEach { step ->
                        Text(step, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 1.dp))
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Select Vyapar Backup", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text(".vyb files (SQLite databases)", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { launcher.launch(arrayOf("application/octet-stream", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Icon(Icons.Filled.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select .vyb File")
                    }
                }
            }

            if (tablesFound.isNotEmpty()) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tables found in database:", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        tablesFound.forEach { Text("\u2022 $it", fontSize = 12.sp, color = Color(0xFF7B1FA2)) }
                    }
                }
            }

            if (isLoading) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(statusMessage.ifBlank { "Importing..." }, color = TextPrimary)
                    }
                }
            }

            if (showResult) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (importedParties + importedItems > 0) GreenBalance.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (importedParties + importedItems > 0) Icons.Filled.CheckCircle else Icons.Filled.Error, contentDescription = null, tint = if (importedParties + importedItems > 0) GreenBalance else RedAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Complete", fontWeight = FontWeight.Bold, color = if (importedParties + importedItems > 0) GreenBalance else RedAccent)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (importedParties > 0) Text("Parties imported: $importedParties", fontSize = 13.sp, color = GreenBalance)
                        if (importedItems > 0) Text("Items imported: $importedItems", fontSize = 13.sp, color = GreenBalance)
                        if (importedParties + importedItems == 0) Text("No data could be imported", fontSize = 13.sp, color = RedAccent)
                        if (errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Errors:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            errors.take(10).forEach { Text(it, fontSize = 11.sp, color = TextSecondary) }
                        }
                    }
                }
            }
        }
    }
}

private fun importPartiesFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM $tableName LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        val nameIdx = columns.indexOfFirst { it.contains("name") || it.contains("party") }
        if (nameIdx == -1) { return Pair(0, listOf("No name column in $tableName")) }

        val phoneIdx = columns.indexOfFirst { it.contains("phone") || it.contains("mobile") || it.contains("contact") }
        val gstinIdx = columns.indexOfFirst { it.contains("gstin") || it.contains("gst") }
        val emailIdx = columns.indexOfFirst { it.contains("email") || it.contains("mail") }
        val addrIdx = columns.indexOfFirst { it.contains("address") || it.contains("addr") }
        val typeIdx = columns.indexOfFirst { it.contains("type") || it.contains("party_type") }
        val stateIdx = columns.indexOfFirst { it.contains("state") }
        val balanceIdx = columns.indexOfFirst { it.contains("balance") || it.contains("opening") }

        val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
        val parties = mutableListOf<PartyEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val name = dataCursor.getString(nameIdx) ?: continue
                if (name.isBlank()) continue
                val type = if (typeIdx >= 0) dataCursor.getString(typeIdx) ?: "" else ""
                val partyType = when {
                    type.contains("supplier") || type.contains("vendor") -> "supplier"
                    type.contains("both") -> "both"
                    else -> "customer"
                }
                parties.add(PartyEntity(
                    companyId = 1L,
                    name = name,
                    phone = if (phoneIdx >= 0) dataCursor.getString(phoneIdx) else null,
                    gstin = if (gstinIdx >= 0) dataCursor.getString(gstinIdx) else null,
                    email = if (emailIdx >= 0) dataCursor.getString(emailIdx) else null,
                    address = if (addrIdx >= 0) dataCursor.getString(addrIdx) else null,
                    state = if (stateIdx >= 0) dataCursor.getString(stateIdx) else null,
                    stateCode = null,
                    balance = if (balanceIdx >= 0) dataCursor.getDouble(balanceIdx) else 0.0,
                    partyType = partyType
                ))
            } catch (e: Exception) { errs.add("Party row: ${e.message}") }
        }
        dataCursor.close()
        val count = runBlocking { viewModel.insertParties(parties) }
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importItemsFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM $tableName LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        val nameIdx = columns.indexOfFirst { it.contains("name") || it.contains("item") || it.contains("product") }
        if (nameIdx == -1) { return Pair(0, listOf("No name column in $tableName")) }

        val priceIdx = columns.indexOfFirst { it.contains("sale_price") || it.contains("selling") || it.contains("price") || it.contains("rate") }
        val purchasePriceIdx = columns.indexOfFirst { it.contains("purchase_price") || it.contains("cost") || it.contains("buying") }
        val hsnIdx = columns.indexOfFirst { it.contains("hsn") || it.contains("sac") }
        val gstIdx = columns.indexOfFirst { it.contains("tax") || it.contains("gst") }
        val unitIdx = columns.indexOfFirst { it.contains("unit") || it.contains("uom") }
        val stockIdx = columns.indexOfFirst { it.contains("stock") || it.contains("quantity") || it.contains("opening") }
        val descIdx = columns.indexOfFirst { it.contains("description") || it.contains("desc") }

        val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
        val items = mutableListOf<ItemEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val name = dataCursor.getString(nameIdx) ?: continue
                if (name.isBlank()) continue
                items.add(ItemEntity(
                    companyId = 1L,
                    name = name,
                    hsnCode = if (hsnIdx >= 0) dataCursor.getString(hsnIdx) else null,
                    description = if (descIdx >= 0) dataCursor.getString(descIdx) else null,
                    salePrice = if (priceIdx >= 0) dataCursor.getDouble(priceIdx) else 0.0,
                    purchasePrice = if (purchasePriceIdx >= 0) dataCursor.getDouble(purchasePriceIdx) else 0.0,
                    gstRate = if (gstIdx >= 0) dataCursor.getDouble(gstIdx) else 0.0,
                    unit = if (unitIdx >= 0) (dataCursor.getString(unitIdx) ?: "NOS") else "NOS",
                    stockQuantity = if (stockIdx >= 0) dataCursor.getDouble(stockIdx) else 0.0,
                    isService = false
                ))
            } catch (e: Exception) { errs.add("Item row: ${e.message}") }
        }
        dataCursor.close()
        val count = runBlocking { viewModel.insertItems(items) }
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun <T> runBlocking(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking { block() }
}

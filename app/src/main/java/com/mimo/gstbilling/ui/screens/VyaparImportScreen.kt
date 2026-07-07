package com.mimo.gstbilling.ui.screens

import android.net.Uri
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabaseCorruptException
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.*
import com.mimo.gstbilling.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream

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
    var importedInvoices by remember { mutableIntStateOf(0) }
    var importedExpenses by remember { mutableIntStateOf(0) }
    var importedTransactions by remember { mutableIntStateOf(0) }
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    var tablesFound by remember { mutableStateOf<List<String>>(emptyList()) }
    var tableDetails by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var showResult by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            isLoading = true
            statusMessage = "Reading file..."
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val resolver = context.contentResolver
                        val inputStream = resolver.openInputStream(it) ?: throw Exception("Cannot open file")

                        // Read first bytes to detect format
                        val headerBytes = ByteArray(16)
                        val headerStream = java.io.BufferedInputStream(inputStream)
                        headerStream.mark(16)
                        val bytesRead = headerStream.read(headerBytes)
                        headerStream.reset()

                        val isZip = bytesRead >= 2 && headerBytes[0] == 0x50.toByte() && headerBytes[1] == 0x4B.toByte()
                        val isSqlite = bytesRead >= 16 && String(headerBytes, 0, 15).contains("SQLite format")
                        // Some .vyb files are raw SQLite without standard header
                        val mightBeRawDb = !isZip && !isSqlite && bytesRead >= 4

                        var tempFile = File(context.cacheDir, "vyapar_import.db")

                        if (isZip) {
                            statusMessage = "Extracting zip archive..."
                            val zipFile = File(context.cacheDir, "vyapar_extracted.db")
                            zipFile.delete()
                            try {
                                extractDbFromZip(headerStream, zipFile)
                                tempFile = zipFile
                            } catch (zipEx: Exception) {
                                // ZIP extraction failed — try raw file as SQLite
                                statusMessage = "ZIP extraction failed, trying raw file..."
                                val rawInput = resolver.openInputStream(it) ?: throw Exception("Cannot reopen file")
                                tempFile.outputStream().use { out -> rawInput.copyTo(out) }
                                rawInput.close()
                            }
                        } else if (isSqlite) {
                            statusMessage = "Reading SQLite database..."
                            tempFile = File(context.cacheDir, "vyapar_import.db")
                            tempFile.outputStream().use { out -> headerStream.copyTo(out) }
                        } else {
                            // Try as raw SQLite or other format
                            statusMessage = "Trying to read as database..."
                            tempFile = File(context.cacheDir, "vyapar_import.db")
                            tempFile.outputStream().use { out -> headerStream.copyTo(out) }
                        }
                        headerStream.close()

                        if (!tempFile.exists() || tempFile.length() == 0L) {
                            throw Exception("Could not extract database from file. The file may be encrypted or in an unsupported format.")
                        }

                        val db: SQLiteDatabase
                        try {
                            db = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
                        } catch (e: Exception) {
                            throw Exception("Cannot open as database: ${e.message}\n\nFile size: ${tempFile.length()} bytes\nFile header: ${headerBytes.take(8).joinToString(" ") { "%02X".format(it) }}")
                        }

                        val allTables = mutableListOf<String>()
                        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null)
                        while (cursor.moveToNext()) {
                            val name = cursor.getString(0)
                            if (!name.startsWith("android_") && !name.startsWith("sqlite_") && !name.startsWith("__")) {
                                allTables.add(name)
                            }
                        }
                        cursor.close()
                        tablesFound = allTables

                        // Get column details for each table
                        val details = mutableMapOf<String, List<String>>()
                        allTables.forEach { table ->
                            try {
                                val colCursor = db.rawQuery("PRAGMA table_info($table)", null)
                                val cols = mutableListOf<String>()
                                while (colCursor.moveToNext()) {
                                    cols.add("${colCursor.getString(colCursor.getColumnIndexOrThrow("name"))} (${colCursor.getString(colCursor.getColumnIndexOrThrow("type"))})")
                                }
                                colCursor.close()
                                details[table] = cols
                            } catch (_: Exception) {}
                        }
                        tableDetails = details

                        val errs = mutableListOf<String>()

                        // Import parties
                        val partyTable = allTables.find { t ->
                            t.contains("party", ignoreCase = true) || t.contains("contact", ignoreCase = true) ||
                            t.contains("customer", ignoreCase = true) || t.contains("supplier", ignoreCase = true) ||
                            t.contains("ledger", ignoreCase = true) || t.contains("account", ignoreCase = true)
                        }
                        if (partyTable != null) {
                            statusMessage = "Importing parties from $partyTable..."
                            val result = importPartiesFromVyaparDb(db, partyTable, viewModel)
                            importedParties = result.first
                            errs.addAll(result.second)
                        }

                        // Import items
                        val itemTable = allTables.find { t ->
                            t.contains("item", ignoreCase = true) || t.contains("product", ignoreCase = true) ||
                            t.contains("inventory", ignoreCase = true) || t.contains("stock", ignoreCase = true) ||
                            t.contains("material", ignoreCase = true)
                        }
                        if (itemTable != null) {
                            statusMessage = "Importing items from $itemTable..."
                            val result = importItemsFromVyaparDb(db, itemTable, viewModel)
                            importedItems = result.first
                            errs.addAll(result.second)
                        }

                        // Import invoices
                        val invoiceTable = allTables.find { t ->
                            t.contains("invoice", ignoreCase = true) || t.contains("bill", ignoreCase = true) ||
                            t.contains("sale", ignoreCase = true) || t.contains("purchase", ignoreCase = true)
                        }
                        if (invoiceTable != null) {
                            statusMessage = "Importing invoices from $invoiceTable..."
                            val result = importInvoicesFromVyaparDb(db, invoiceTable, viewModel)
                            importedInvoices = result.first
                            errs.addAll(result.second)
                        }

                        // Import expenses
                        val expenseTable = allTables.find { t ->
                            t.contains("expense", ignoreCase = true) || t.contains("payment_made", ignoreCase = true)
                        }
                        if (expenseTable != null) {
                            statusMessage = "Importing expenses from $expenseTable..."
                            val result = importExpensesFromVyaparDb(db, expenseTable, viewModel)
                            importedExpenses = result.first
                            errs.addAll(result.second)
                        }

                        // Import payments
                        val paymentTable = allTables.find { t ->
                            (t.contains("payment", ignoreCase = true) || t.contains("receipt", ignoreCase = true) ||
                            t.contains("transaction", ignoreCase = true)) && !t.contains("payment_mode", ignoreCase = true)
                        }
                        if (paymentTable != null) {
                            statusMessage = "Importing payments from $paymentTable..."
                            val result = importTransactionsFromVyaparDb(db, paymentTable, viewModel)
                            importedTransactions = result.first
                            errs.addAll(result.second)
                        }

                        val totalImported = importedParties + importedItems + importedInvoices + importedExpenses + importedTransactions
                        if (totalImported == 0 && allTables.isNotEmpty()) {
                            errs.add("Tables found but no matching data columns detected.")
                            errs.add("Found tables: ${allTables.joinToString(", ")}")
                        }
                        if (allTables.isEmpty()) {
                            errs.add("No business tables found in the file.")
                            errs.add("The file may be encrypted or in a different format.")
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
            // Header card
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp).background(Color(0xFF1B5E20).copy(alpha = 0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Import from Vyapar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Import parties, items, invoices, expenses, and payments", fontSize = 13.sp, color = TextSecondary)
                }
            }

            // What will be imported
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Data that will be imported:", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "\u2713 Parties (Customers & Suppliers)" to GreenBalance,
                        "\u2713 Items (Products & Services)" to GreenBalance,
                        "\u2713 Invoices (Sales & Purchase)" to GreenBalance,
                        "\u2713 Expenses" to GreenBalance,
                        "\u2713 Payments & Transactions" to GreenBalance
                    ).forEach { (item, color) ->
                        Text(item, fontSize = 13.sp, color = color, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            // How to export
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("How to export from Vyapar:", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "1. Open Vyapar app",
                        "2. Go to Settings > Backup & Restore",
                        "3. Tap 'Backup to phone'",
                        "4. Save the backup file",
                        "5. Transfer file to this phone (WhatsApp/Email/File Manager)",
                        "6. Tap button below to import"
                    ).forEach { step ->
                        Text(step, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 1.dp))
                    }
                }
            }

            // Import button
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = Color(0xFF1B5E20), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Select Backup File", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Accepts .vyb, .db, .sqlite, .zip files", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { launcher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Icon(Icons.Filled.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Backup File")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tip: If .vyb doesn't open, try renaming it to .db or .zip first",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Fetch from URL button
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudSync, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fetch from Anyclaw", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Download Arihant billing data directly", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            statusMessage = "Fetching data from Anyclaw..."
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        val url = java.net.URL("https://arihant-billing.anyclaw.store/data.js")
                                        val connection = url.openConnection()
                                        connection.connectTimeout = 30000
                                        connection.readTimeout = 60000
                                        val inputStream = connection.getInputStream()
                                        val dataJs = inputStream.bufferedReader().use { it.readText() }
                                        inputStream.close()

                                        statusMessage = "Parsing billing data..."
                                        parseAndImportDataJs(dataJs, viewModel) { msg, parties, items, invoices, expenses, txns ->
                                            importedParties = parties
                                            importedItems = items
                                            importedInvoices = invoices
                                            importedExpenses = expenses
                                            importedTransactions = txns
                                            statusMessage = msg
                                        }
                                    }
                                    showResult = true
                                } catch (e: Exception) {
                                    errors = listOf("Error: ${e.message}")
                                    showResult = true
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetch & Import Data")
                    }
                }
            }

            // Tables found
            if (tablesFound.isNotEmpty()) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tables found: ${tablesFound.size}", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        tablesFound.forEach { table ->
                            var expanded by remember { mutableStateOf(false) }
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = null,
                                        tint = Color(0xFF1B5E20),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(table, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    tableDetails[table]?.let { cols ->
                                        Text(" (${cols.size} cols)", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                                if (expanded) {
                                    tableDetails[table]?.forEach { col ->
                                        Text("  \u2022 $col", fontSize = 11.sp, color = Color(0xFF4CAF50), modifier = Modifier.padding(start = 22.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loading
            if (isLoading) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(statusMessage.ifBlank { "Importing..." }, color = TextPrimary)
                    }
                }
            }

            // Results
            if (showResult) {
                val totalImported = importedParties + importedItems + importedInvoices + importedExpenses + importedTransactions
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (totalImported > 0) GreenBalance.copy(alpha = 0.1f) else Color(0xFFFFF3E0))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (totalImported > 0) Icons.Filled.CheckCircle else Icons.Filled.Warning, contentDescription = null, tint = if (totalImported > 0) GreenBalance else Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Result", fontWeight = FontWeight.Bold, color = if (totalImported > 0) GreenBalance else Color(0xFFFF9800), fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (importedParties > 0) Text("\u2713 Parties imported: $importedParties", fontSize = 13.sp, color = GreenBalance)
                        if (importedItems > 0) Text("\u2713 Items imported: $importedItems", fontSize = 13.sp, color = GreenBalance)
                        if (importedInvoices > 0) Text("\u2713 Invoices imported: $importedInvoices", fontSize = 13.sp, color = GreenBalance)
                        if (importedExpenses > 0) Text("\u2713 Expenses imported: $importedExpenses", fontSize = 13.sp, color = GreenBalance)
                        if (importedTransactions > 0) Text("\u2713 Payments imported: $importedTransactions", fontSize = 13.sp, color = GreenBalance)
                        if (totalImported == 0) {
                            Text("No data could be imported", fontSize = 13.sp, color = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Possible reasons:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("\u2022 File may be encrypted by Vyapar", fontSize = 12.sp, color = TextSecondary)
                            Text("\u2022 File format not recognized", fontSize = 12.sp, color = TextSecondary)
                            Text("\u2022 Try exporting as CSV from Vyapar instead", fontSize = 12.sp, color = TextSecondary)
                        }
                        if (errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Details:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            errors.take(20).forEach { Text(it, fontSize = 11.sp, color = TextSecondary) }
                        }
                    }
                }
            }
        }
    }
}

private fun extractDbFromZip(inputStream: java.io.BufferedInputStream, outputFile: File) {
    val zis = ZipInputStream(inputStream)
    var entry = zis.nextEntry
    val entries = mutableListOf<Pair<String, Long>>()

    // First pass: collect all entries with their sizes
    while (entry != null) {
        if (!entry.isDirectory) {
            entries.add(Pair(entry.name, entry.size))
        }
        zis.closeEntry()
        entry = zis.nextEntry
    }
    zis.close()

    // Re-open for extraction
    val zis2 = ZipInputStream(java.io.BufferedInputStream(inputStream))
    var entry2 = zis2.nextEntry
    while (entry2 != null) {
        val name = entry2.name.lowercase()
        // Match by extension OR by common database names
        val isDbFile = name.endsWith(".db") || name.endsWith(".sqlite") || name.endsWith(".sqlite3") ||
                name.endsWith(".vyb") || name.endsWith(".sql") ||
                name.contains("database") || name.contains("backup") || name.contains("data")

        // If single entry, extract it regardless of name
        val isSingleEntry = entries.size == 1

        if (isDbFile || isSingleEntry) {
            outputFile.outputStream().use { out ->
                zis2.copyTo(out)
            }
            zis2.closeEntry()
            zis2.close()
            return
        }
        zis2.closeEntry()
        entry2 = zis2.nextEntry
    }
    zis2.close()
    throw Exception("No database file found inside the zip archive")
}

private fun importPartiesFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM `$tableName` LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        errs.add("Table: $tableName, Columns: ${columns.joinToString(", ")}")

        val nameIdx = columns.indexOfFirst { it.contains("name") || it.contains("party") || it.contains("ledger") || it.contains("contact") }
        if (nameIdx == -1) { return Pair(0, errs + "No name column found in $tableName") }

        val phoneIdx = columns.indexOfFirst { it.contains("phone") || it.contains("mobile") || it.contains("contact") && it.contains("number") }
        val gstinIdx = columns.indexOfFirst { it.contains("gstin") || it.contains("gst_number") || it.contains("tax_number") || it.contains("gstin_number") }
        val emailIdx = columns.indexOfFirst { it.contains("email") || it.contains("mail") }
        val addrIdx = columns.indexOfFirst { it.contains("address") || it.contains("addr") || it.contains("billing") }
        val typeIdx = columns.indexOfFirst { it.contains("type") || it.contains("party_type") || it.contains("ledger_type") || it.contains("category") }
        val stateIdx = columns.indexOfFirst { it.contains("state") }
        val balanceIdx = columns.indexOfFirst { it.contains("balance") || it.contains("opening") || it.contains("current") }
        val cityIdx = columns.indexOfFirst { it.contains("city") || it.contains("district") || it.contains("town") }
        val pincodeIdx = columns.indexOfFirst { it.contains("pincode") || it.contains("pin_code") || it.contains("zip") || it.contains("postal") }
        val phone2Idx = columns.indexOfFirst { it.contains("phone") && it != columns.getOrNull(phoneIdx)?.lowercase() }

        val actualPhoneIdx = if (phoneIdx >= 0) phoneIdx else phone2Idx

        val dataCursor = db.rawQuery("SELECT * FROM `$tableName`", null)
        val parties = mutableListOf<PartyEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val name = dataCursor.getString(nameIdx) ?: continue
                if (name.isBlank() || name.lowercase() == "null") continue
                val type = if (typeIdx >= 0) dataCursor.getString(typeIdx) ?: "" else ""
                val partyType = when {
                    type.contains("supplier") || type.contains("vendor") || type.contains("creditor") || type.contains("purchase") -> "supplier"
                    type.contains("both") -> "both"
                    else -> "customer"
                }
                val address = buildString {
                    if (addrIdx >= 0) dataCursor.getString(addrIdx)?.let { if (it != "null") append(it) }
                    if (cityIdx >= 0) dataCursor.getString(cityIdx)?.let { if (it != "null" && it.isNotBlank()) { if (isNotEmpty()) append(", "); append(it) } }
                    if (pincodeIdx >= 0) dataCursor.getString(pincodeIdx)?.let { if (it != "null" && it.isNotBlank()) { if (isNotEmpty()) append(" - "); append(it) } }
                }.ifBlank { null }

                parties.add(PartyEntity(
                    companyId = 1L,
                    name = name.trim(),
                    phone = getColValue(dataCursor, actualPhoneIdx),
                    gstin = getColValue(dataCursor, gstinIdx),
                    email = getColValue(dataCursor, emailIdx),
                    address = address,
                    state = getColValue(dataCursor, stateIdx),
                    stateCode = null,
                    balance = getColDouble(dataCursor, balanceIdx),
                    partyType = partyType
                ))
            } catch (e: Exception) { errs.add("Party row error: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertParties(parties) }
        errs.add("Imported $count parties from $tableName")
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importItemsFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM `$tableName` LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        errs.add("Table: $tableName, Columns: ${columns.joinToString(", ")}")

        val nameIdx = columns.indexOfFirst { it.contains("name") || it.contains("item") || it.contains("product") || it.contains("material") }
        if (nameIdx == -1) { return Pair(0, errs + "No name column found in $tableName") }

        val priceIdx = columns.indexOfFirst { it.contains("sale_price") || it.contains("selling") || it.contains("price") || it.contains("rate") || it.contains("mrp") }
        val purchasePriceIdx = columns.indexOfFirst { it.contains("purchase_price") || it.contains("cost") || it.contains("buying") || it.contains("purchase_rate") }
        val hsnIdx = columns.indexOfFirst { it.contains("hsn") || it.contains("sac") || it.contains("hsn_code") }
        val gstIdx = columns.indexOfFirst { it.contains("tax") || it.contains("gst") || it.contains("tax_rate") || it.contains("gst_rate") || it.contains("tax percentage") }
        val unitIdx = columns.indexOfFirst { it.contains("unit") || it.contains("uom") || it.contains("unit_name") || it.contains("measurement") }
        val stockIdx = columns.indexOfFirst { it.contains("stock") || it.contains("quantity") || it.contains("opening_stock") || it.contains("balance") || it.contains("current_stock") }
        val descIdx = columns.indexOfFirst { it.contains("description") || it.contains("desc") || it.contains("details") || it.contains("note") }
        val skuIdx = columns.indexOfFirst { it.contains("sku") || it.contains("code") || it.contains("item_code") || it.contains("barcode") }

        val dataCursor = db.rawQuery("SELECT * FROM `$tableName`", null)
        val items = mutableListOf<ItemEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val name = dataCursor.getString(nameIdx) ?: continue
                if (name.isBlank() || name.lowercase() == "null") continue
                val gstRate = getGstRate(dataCursor, gstIdx)

                items.add(ItemEntity(
                    companyId = 1L,
                    name = name.trim(),
                    hsnCode = getColValue(dataCursor, hsnIdx),
                    description = getColValue(dataCursor, descIdx),
                    salePrice = getColDouble(dataCursor, priceIdx),
                    purchasePrice = getColDouble(dataCursor, purchasePriceIdx),
                    gstRate = gstRate,
                    unit = getColValue(dataCursor, unitIdx) ?: "NOS",
                    stockQuantity = getColDouble(dataCursor, stockIdx),
                    isService = false
                ))
            } catch (e: Exception) { errs.add("Item row error: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertItems(items) }
        errs.add("Imported $count items from $tableName")
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importInvoicesFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM `$tableName` LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        errs.add("Table: $tableName, Columns: ${columns.joinToString(", ")}")

        val numIdx = columns.indexOfFirst { it.contains("number") || it.contains("invoice_number") || it.contains("bill_number") || it.contains("ref") || it.contains("no") }
        if (numIdx == -1) { return Pair(0, errs + "No invoice number column found in $tableName") }

        val dateIdx = columns.indexOfFirst { it.contains("date") || it.contains("invoice_date") || it.contains("bill_date") || it.contains("created") }
        val totalIdx = columns.indexOfFirst { it.contains("total") || it.contains("grand_total") || it.contains("amount") || it.contains("net_amount") || it.contains("bill_amount") || it.contains("total_amount") }
        val taxIdx = columns.indexOfFirst { it.contains("tax") || it.contains("gst") || it.contains("tax_amount") || it.contains("total_tax") }
        val cgstIdx = columns.indexOfFirst { it.contains("cgst") || it.contains("central_tax") || it.contains("cgst_amount") }
        val sgstIdx = columns.indexOfFirst { it.contains("sgst") || it.contains("state_tax") || it.contains("sgst_amount") }
        val igstIdx = columns.indexOfFirst { it.contains("igst") || it.contains("integrated_tax") || it.contains("igst_amount") }
        val discountIdx = columns.indexOfFirst { it.contains("discount") || it.contains("discount_amount") || it.contains("discount_value") }
        val typeIdx = columns.indexOfFirst { it.contains("type") || it.contains("invoice_type") || it.contains("bill_type") }
        val statusIdx = columns.indexOfFirst { it.contains("status") || it.contains("payment_status") || it.contains("payment_state") || it.contains("state") }
        val taxableIdx = columns.indexOfFirst { it.contains("taxable") || it.contains("taxable_amount") || it.contains("subtotal") || it.contains("net_total") || it.contains("sub_total") }

        val dataCursor = db.rawQuery("SELECT * FROM `$tableName`", null)
        val invoices = mutableListOf<InvoiceEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val invNum = dataCursor.getString(numIdx) ?: continue
                if (invNum.isBlank() || invNum.lowercase() == "null") continue

                val dateMillis = parseDate(dataCursor.getString(dateIdx) ?: "")
                val total = getColDouble(dataCursor, totalIdx)
                val taxable = getColDouble(dataCursor, taxableIdx).let { if (it > 0) it else total }
                val cgst = getColDouble(dataCursor, cgstIdx)
                val sgst = getColDouble(dataCursor, sgstIdx)
                val igst = getColDouble(dataCursor, igstIdx)
                val tax = getColDouble(dataCursor, taxIdx)
                val discount = getColDouble(dataCursor, discountIdx)

                val invType = if (typeIdx >= 0) {
                    val t = dataCursor.getString(typeIdx) ?: "sale"
                    if (t.contains("purchase") || t.contains("buy")) "purchase" else "sales"
                } else "sales"

                val status = if (statusIdx >= 0) {
                    val s = dataCursor.getString(statusIdx) ?: "unpaid"
                    when {
                        s.contains("paid") || s.contains("complete") || s.contains("cleared") -> "paid"
                        s.contains("partial") || s.contains("part") -> "partial"
                        else -> "unpaid"
                    }
                } else "unpaid"

                val calculatedCgst = if (cgst > 0) cgst else if (tax > 0) tax / 2 else 0.0
                val calculatedSgst = if (sgst > 0) sgst else if (tax > 0) tax / 2 else 0.0

                invoices.add(InvoiceEntity(
                    companyId = 1L,
                    partyId = 0L,
                    invoiceNumber = invNum.trim(),
                    invoiceDate = dateMillis,
                    dueDate = null,
                    subTotal = taxable,
                    discount = discount,
                    taxableAmount = taxable,
                    cgstTotal = calculatedCgst,
                    sgstTotal = calculatedSgst,
                    igstTotal = igst,
                    totalAmount = total,
                    paymentStatus = status,
                    invoiceType = invType
                ))
            } catch (e: Exception) { errs.add("Invoice row error: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertInvoices(invoices) }
        errs.add("Imported $count invoices from $tableName")
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importExpensesFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM `$tableName` LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        errs.add("Table: $tableName, Columns: ${columns.joinToString(", ")}")

        val amountIdx = columns.indexOfFirst { it.contains("amount") || it.contains("expense_amount") || it.contains("total") || it.contains("value") }
        if (amountIdx == -1) { return Pair(0, errs + "No amount column found in $tableName") }

        val dateIdx = columns.indexOfFirst { it.contains("date") || it.contains("expense_date") || it.contains("created") }
        val catIdx = columns.indexOfFirst { it.contains("category") || it.contains("expense_category") || it.contains("type") || it.contains("expense_type") || it.contains("head") }
        val descIdx = columns.indexOfFirst { it.contains("description") || it.contains("desc") || it.contains("note") || it.contains("detail") || it.contains("particular") }
        val modeIdx = columns.indexOfFirst { it.contains("mode") || it.contains("payment_mode") || it.contains("payment_type") || it.contains("method") }

        val dataCursor = db.rawQuery("SELECT * FROM `$tableName`", null)
        val expenses = mutableListOf<ExpenseEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val amount = getColDouble(dataCursor, amountIdx)
                if (amount <= 0) continue

                expenses.add(ExpenseEntity(
                    companyId = 1L,
                    category = getColValue(dataCursor, catIdx) ?: "General",
                    amount = amount,
                    date = parseDate(dataCursor.getString(dateIdx) ?: ""),
                    description = getColValue(dataCursor, descIdx),
                    paymentMode = getColValue(dataCursor, modeIdx) ?: "cash"
                ))
            } catch (e: Exception) { errs.add("Expense row error: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertExpenses(expenses) }
        errs.add("Imported $count expenses from $tableName")
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importTransactionsFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM `$tableName` LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        errs.add("Table: $tableName, Columns: ${columns.joinToString(", ")}")

        val amountIdx = columns.indexOfFirst { it.contains("amount") || it.contains("payment_amount") || it.contains("received") || it.contains("paid") || it.contains("value") }
        if (amountIdx == -1) { return Pair(0, errs + "No amount column found in $tableName") }

        val dateIdx = columns.indexOfFirst { it.contains("date") || it.contains("payment_date") || it.contains("created") || it.contains("transaction_date") }
        val typeIdx = columns.indexOfFirst { it.contains("type") || it.contains("payment_type") || it.contains("transaction_type") || it.contains("mode") }
        val modeIdx = columns.indexOfFirst { it.contains("mode") || it.contains("payment_mode") || it.contains("method") || it.contains("instrument") }
        val descIdx = columns.indexOfFirst { it.contains("description") || it.contains("note") || it.contains("reference") || it.contains("narration") || it.contains("particular") }

        val dataCursor = db.rawQuery("SELECT * FROM `$tableName`", null)
        val transactions = mutableListOf<TransactionEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val amount = getColDouble(dataCursor, amountIdx)
                if (amount <= 0) continue

                val type = if (typeIdx >= 0) {
                    val t = dataCursor.getString(typeIdx) ?: "credit"
                    if (t.contains("receive") || t.contains("in") || t.contains("credit") || t.contains("received")) "credit" else "debit"
                } else "debit"

                transactions.add(TransactionEntity(
                    companyId = 1L,
                    partyId = 0L,
                    amount = amount,
                    type = type,
                    mode = getColValue(dataCursor, modeIdx) ?: "cash",
                    description = getColValue(dataCursor, descIdx),
                    date = parseDate(dataCursor.getString(dateIdx) ?: "")
                ))
            } catch (e: Exception) { errs.add("Transaction row error: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertTransactions(transactions) }
        errs.add("Imported $count transactions from $tableName")
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun getColValue(cursor: android.database.Cursor, idx: Int): String? {
    if (idx < 0) return null
    return try {
        val value = cursor.getString(idx)
        if (value == "null" || value.isNullOrBlank()) null else value.trim()
    } catch (_: Exception) { null }
}

private fun getColDouble(cursor: android.database.Cursor, idx: Int): Double {
    if (idx < 0) return 0.0
    return try {
        cursor.getDouble(idx)
    } catch (_: Exception) {
        try {
            cursor.getString(idx)?.replace("[^0-9.-]", "")?.toDoubleOrNull() ?: 0.0
        } catch (_: Exception) { 0.0 }
    }
}

private fun getGstRate(cursor: android.database.Cursor, idx: Int): Double {
    if (idx < 0) return 0.0
    return try {
        val raw = cursor.getString(idx) ?: return 0.0
        raw.replace("%", "").trim().toDoubleOrNull() ?: 0.0
    } catch (_: Exception) { 0.0 }
}

private fun parseDate(dateStr: String): Long {
    if (dateStr.isBlank() || dateStr.lowercase() == "null") return System.currentTimeMillis()
    try {
        val cleaned = dateStr.split(" ")[0]
        val formats = listOf(
            "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "MM/dd/yyyy",
            "dd-MMM-yyyy", "MMM dd, yyyy", "yyyy/MM/dd", "dd.MM.yyyy"
        )
        for (fmt in formats) {
            try {
                java.text.SimpleDateFormat(fmt, java.util.Locale.US).parse(cleaned)?.time?.let { return it }
            } catch (_: Exception) {}
        }
    } catch (_: Exception) {}
    return System.currentTimeMillis()
}

private fun parseAndImportDataJs(
    dataJs: String,
    viewModel: ImportViewModel,
    onProgress: (String, Int, Int, Int, Int, Int) -> Unit
) {
    // Extract columns and rows for each table from the JS
    fun extractTable(tableName: String): Pair<List<String>, List<List<String>>>? {
        // Find "tableName": { "columns": [...], "rows": [...] }
        val tablePattern = Regex("\"$tableName\"\\s*:\\s*\\{\\s*\"columns\"\\s*:\\s*\\[([^\\]]+)\\]\\s*,\\s*\"rows\"\\s*:\\s*\\[([^\\]]*)\\]")
        val match = tablePattern.find(dataJs) ?: return null

        val columnsStr = match.groupValues[1]
        val columns = Regex("\"([^\"]+)\"").findAll(columnsStr).map { it.groupValues[1] }.toList()

        val rowsStr = match.groupValues[2]
        val rows = mutableListOf<List<String>>()
        val rowPattern = Regex("\\[([^\\]]*)\\]")
        for (rowMatch in rowPattern.findAll(rowsStr)) {
            val cells = Regex("\"([^\"]*?)\"|([0-9.]+)").findAll(rowMatch.groupValues[1])
                .map { it.groupValues[1].ifEmpty { it.groupValues[2] } }
                .toList()
            rows.add(cells)
        }
        return Pair(columns, rows)
    }

    fun getColIndex(columns: List<String>, vararg names: String): Int {
        return columns.indexOfFirst { col -> names.any { col.equals(it, ignoreCase = true) } }
    }

    fun getString(row: List<String>, columns: List<String>, vararg names: String): String? {
        val idx = getColIndex(columns, *names)
        if (idx < 0 || idx >= row.size) return null
        val v = row[idx]
        return if (v.isBlank() || v == "null") null else v.trim()
    }

    fun getDouble(row: List<String>, columns: List<String>, vararg names: String): Double {
        val idx = getColIndex(columns, *names)
        if (idx < 0 || idx >= row.size) return 0.0
        return row[idx].replace(",", "").toDoubleOrNull() ?: 0.0
    }

    var totalParties = 0
    var totalItems = 0
    var totalInvoices = 0
    var totalExpenses = 0
    var totalTxns = 0

    // Import firm details into company
    val firmData = extractTable("kb_firms") ?: extractTable("firm_details")
    firmData?.let { (cols, rows) ->
        if (rows.isNotEmpty()) {
            val row = rows[0]
            val name = getString(row, cols, "firm_name") ?: "My Business"
            val gstin = getString(row, cols, "firm_gstin_number")
            val phone = getString(row, cols, "firm_phone")
            val email = getString(row, cols, "firm_email")
            val address = getString(row, cols, "firm_address")
            val state = getString(row, cols, "firm_state")
            val bankName = getString(row, cols, "firm_bank_name")
            val bankAcct = getString(row, cols, "firm_bank_account_number")
            val bankIfsc = getString(row, cols, "firm_bank_ifsc_code")
            val desc = getString(row, cols, "firm_description")

            val company = com.mimo.gstbilling.data.local.entity.CompanyEntity(
                name = name,
                gstin = gstin,
                address = address,
                phone = phone,
                email = email,
                businessType = getString(row, cols, "firm_business_type"),
                state = state,
                stateCode = null,
                logoUri = null,
                signatureUri = null,
                bankName = bankName,
                bankAccountNumber = bankAcct,
                bankIfsc = bankIfsc,
                bankBranch = null,
                bankUpiId = null,
                termsAndConditions = null,
                declaration = null,
                msmeUdyamNumber = null
            )
            kotlinx.coroutines.runBlocking { viewModel.insertCompany(company) }
        }
    }

    // Import parties
    val namesData = extractTable("kb_names")
    namesData?.let { (cols, rows) ->
        val parties = mutableListOf<com.mimo.gstbilling.data.local.entity.PartyEntity>()
        for (row in rows) {
            val nameType = getString(row, cols, "name_type") ?: "1"
            if (nameType != "1") continue // Only customers, skip expense categories
            val name = getString(row, cols, "full_name") ?: continue
            if (name.isBlank()) continue

            val phone = getString(row, cols, "phone_number")
            val gstin = getString(row, cols, "name_gstin_number")
            val email = getString(row, cols, "email")
            val address = getString(row, cols, "address")
            val state = getString(row, cols, "name_state")

            parties.add(com.mimo.gstbilling.data.local.entity.PartyEntity(
                companyId = 1L,
                name = name,
                phone = phone,
                gstin = gstin,
                email = email,
                address = address,
                state = state,
                stateCode = null,
                balance = getDouble(row, cols, "amount"),
                partyType = "customer"
            ))
        }
        totalParties = kotlinx.coroutines.runBlocking { viewModel.insertParties(parties) }
        onProgress("Imported $totalParties parties", totalParties, totalItems, totalInvoices, totalExpenses, totalTxns)
    }

    // Import items
    val itemsData = extractTable("kb_items")
    itemsData?.let { (cols, rows) ->
        val items = mutableListOf<com.mimo.gstbilling.data.local.entity.ItemEntity>()
        for (row in rows) {
            val name = getString(row, cols, "item_name") ?: continue
            if (name.isBlank()) continue

            val gstId = getString(row, cols, "item_tax_id") ?: ""
            val gstRate = when (gstId) {
                "24" -> 18.0
                "16" -> 5.0
                "4" -> 0.0
                else -> 0.0
            }

            items.add(com.mimo.gstbilling.data.local.entity.ItemEntity(
                companyId = 1L,
                name = name,
                hsnCode = getString(row, cols, "item_hsn_sac_code"),
                description = getString(row, cols, "item_description"),
                salePrice = getDouble(row, cols, "item_sale_unit_price"),
                purchasePrice = getDouble(row, cols, "item_purchase_unit_price"),
                gstRate = gstRate,
                unit = "NOS",
                stockQuantity = getDouble(row, cols, "item_stock_quantity"),
                isService = getString(row, cols, "item_type") == "2"
            ))
        }
        totalItems = kotlinx.coroutines.runBlocking { viewModel.insertItems(items) }
        onProgress("Imported $totalParties parties, $totalItems items", totalParties, totalItems, totalInvoices, totalExpenses, totalTxns)
    }

    // Import transactions (invoices)
    val txnData = extractTable("kb_transactions")
    val namesTable = extractTable("kb_names")
    val nameMap = mutableMapOf<String, Long>() // name_id -> partyId (we'll use name_id as reference)

    txnData?.let { (cols, rows) ->
        val invoices = mutableListOf<com.mimo.gstbilling.data.local.entity.InvoiceEntity>()
        for (row in rows) {
            val invNum = getString(row, cols, "txn_ref_number_char") ?: continue
            if (invNum.isBlank()) continue

            val txnType = getString(row, cols, "txn_type") ?: "1"
            if (txnType != "1" && txnType != "3") continue // Only sales and returns

            val dateMillis = parseDate(getString(row, cols, "txn_date") ?: "")
            val cashPaid = getDouble(row, cols, "txn_cash_amount")
            val balance = getDouble(row, cols, "txn_balance_amount")
            val total = cashPaid + balance
            val discount = getDouble(row, cols, "txn_discount_amount")
            val tax = getDouble(row, cols, "txn_tax_amount")
            val subTotal = total - tax

            val invType = if (txnType == "3") "sales_return" else "sales"
            val status = when (getString(row, cols, "txn_payment_status")) {
                "3" -> "paid"
                "2" -> "partial"
                else -> "unpaid"
            }

            invoices.add(com.mimo.gstbilling.data.local.entity.InvoiceEntity(
                companyId = 1L,
                partyId = 0L,
                invoiceNumber = invNum.trim(),
                invoiceDate = dateMillis,
                dueDate = null,
                subTotal = subTotal,
                discount = discount,
                taxableAmount = subTotal,
                cgstTotal = tax / 2,
                sgstTotal = tax / 2,
                igstTotal = 0.0,
                totalAmount = total,
                paymentStatus = status,
                invoiceType = invType
            ))
        }
        totalInvoices = kotlinx.coroutines.runBlocking { viewModel.insertInvoices(invoices) }
        totalTxns = totalInvoices
        onProgress("Imported $totalParties parties, $totalItems items, $totalInvoices invoices", totalParties, totalItems, totalInvoices, totalExpenses, totalTxns)
    }
}

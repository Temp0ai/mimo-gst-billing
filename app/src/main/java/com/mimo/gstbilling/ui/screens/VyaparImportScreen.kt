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
import com.mimo.gstbilling.data.local.entity.*
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
    var importedInvoices by remember { mutableIntStateOf(0) }
    var importedExpenses by remember { mutableIntStateOf(0) }
    var importedTransactions by remember { mutableIntStateOf(0) }
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

                        // Import parties
                        val partyTable = allTables.find { t ->
                            t.contains("party", ignoreCase = true) || t.contains("contact", ignoreCase = true) ||
                            t.contains("customer", ignoreCase = true) || t.contains("supplier", ignoreCase = true) ||
                            t.contains("ledger", ignoreCase = true)
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
                            t.contains("inventory", ignoreCase = true) || t.contains("stock", ignoreCase = true)
                        }
                        if (itemTable != null) {
                            statusMessage = "Importing items from $itemTable..."
                            val result = importItemsFromVyaparDb(db, itemTable, viewModel)
                            importedItems = result.first
                            errs.addAll(result.second)
                        }

                        // Import invoices/bills
                        val invoiceTable = allTables.find { t ->
                            t.contains("invoice", ignoreCase = true) || t.contains("bill", ignoreCase = true) ||
                            t.contains("sale", ignoreCase = true) || t.contains("purchase", ignoreCase = true) ||
                            t.contains("transaction", ignoreCase = true) && !t.contains("payment", ignoreCase = true)
                        }
                        if (invoiceTable != null) {
                            statusMessage = "Importing invoices from $invoiceTable..."
                            val result = importInvoicesFromVyaparDb(db, invoiceTable, allTables, viewModel)
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

                        // Import payments/transactions
                        val paymentTable = allTables.find { t ->
                            (t.contains("payment", ignoreCase = true) || t.contains("receipt", ignoreCase = true)) &&
                            !t.contains("payment_mode", ignoreCase = true)
                        }
                        if (paymentTable != null) {
                            statusMessage = "Importing payments from $paymentTable..."
                            val result = importTransactionsFromVyaparDb(db, paymentTable, viewModel)
                            importedTransactions = result.first
                            errs.addAll(result.second)
                        }

                        val totalImported = importedParties + importedItems + importedInvoices + importedExpenses + importedTransactions
                        if (totalImported == 0 && allTables.isNotEmpty()) {
                            errs.add("Tables found but no matching data columns. Tables: ${allTables.joinToString(", ")}")
                        }
                        if (allTables.isEmpty()) {
                            errs.add("No tables found in the database file")
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
                    Text("Import from Vyapar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Import parties, items, invoices, expenses, and payments from Vyapar .vyb backup files", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("What will be imported:", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "\u2022 Parties (Customers & Suppliers)" to GreenBalance,
                        "\u2022 Items (Products & Services)" to GreenBalance,
                        "\u2022 Invoices (Sales & Purchase)" to GreenBalance,
                        "\u2022 Expenses" to GreenBalance,
                        "\u2022 Payments & Transactions" to GreenBalance
                    ).forEach { (item, color) ->
                        Text(item, fontSize = 13.sp, color = color, modifier = Modifier.padding(vertical = 2.dp))
                    }
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
                        Text("Tables found in database: (${tablesFound.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tablesFound.joinToString(", "), fontSize = 12.sp, color = Color(0xFF7B1FA2))
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
                val totalImported = importedParties + importedItems + importedInvoices + importedExpenses + importedTransactions
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (totalImported > 0) GreenBalance.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (totalImported > 0) Icons.Filled.CheckCircle else Icons.Filled.Error, contentDescription = null, tint = if (totalImported > 0) GreenBalance else RedAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Complete", fontWeight = FontWeight.Bold, color = if (totalImported > 0) GreenBalance else RedAccent, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (importedParties > 0) Text("\u2713 Parties: $importedParties", fontSize = 13.sp, color = GreenBalance)
                        if (importedItems > 0) Text("\u2713 Items: $importedItems", fontSize = 13.sp, color = GreenBalance)
                        if (importedInvoices > 0) Text("\u2713 Invoices: $importedInvoices", fontSize = 13.sp, color = GreenBalance)
                        if (importedExpenses > 0) Text("\u2713 Expenses: $importedExpenses", fontSize = 13.sp, color = GreenBalance)
                        if (importedTransactions > 0) Text("\u2713 Payments: $importedTransactions", fontSize = 13.sp, color = GreenBalance)
                        if (totalImported == 0) Text("No data could be imported", fontSize = 13.sp, color = RedAccent)
                        if (errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Notes:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            errors.take(15).forEach { Text(it, fontSize = 11.sp, color = TextSecondary) }
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

        val nameIdx = columns.indexOfFirst { it.contains("name") || it.contains("party") || it.contains("ledger_name") }
        if (nameIdx == -1) { return Pair(0, listOf("No name column in $tableName")) }

        val phoneIdx = columns.indexOfFirst { it.contains("phone") || it.contains("mobile") || it.contains("contact") }
        val gstinIdx = columns.indexOfFirst { it.contains("gstin") || it.contains("gst_number") || it.contains("tax_number") }
        val emailIdx = columns.indexOfFirst { it.contains("email") || it.contains("mail") }
        val addrIdx = columns.indexOfFirst { it.contains("address") || it.contains("addr") || it.contains("billing_address") }
        val typeIdx = columns.indexOfFirst { it.contains("type") || it.contains("party_type") || it.contains("ledger_type") }
        val stateIdx = columns.indexOfFirst { it.contains("state") }
        val balanceIdx = columns.indexOfFirst { it.contains("balance") || it.contains("opening") || it.contains("current_balance") }
        val cityIdx = columns.indexOfFirst { it.contains("city") || it.contains("district") }
        val pincodeIdx = columns.indexOfFirst { it.contains("pincode") || it.contains("pin_code") || it.contains("zip") }

        val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
        val parties = mutableListOf<PartyEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val name = dataCursor.getString(nameIdx) ?: continue
                if (name.isBlank()) continue
                val type = if (typeIdx >= 0) dataCursor.getString(typeIdx) ?: "" else ""
                val partyType = when {
                    type.contains("supplier") || type.contains("vendor") || type.contains("creditor") -> "supplier"
                    type.contains("both") -> "both"
                    else -> "customer"
                }
                val address = buildString {
                    if (addrIdx >= 0) dataCursor.getString(addrIdx)?.let { append(it) }
                    if (cityIdx >= 0) dataCursor.getString(cityIdx)?.let { if (isNotEmpty()) append(", "); append(it) }
                    if (pincodeIdx >= 0) dataCursor.getString(pincodeIdx)?.let { if (isNotEmpty()) append(" - "); append(it) }
                }.ifBlank { null }

                parties.add(PartyEntity(
                    companyId = 1L,
                    name = name.trim(),
                    phone = if (phoneIdx >= 0) dataCursor.getString(phoneIdx)?.trim() else null,
                    gstin = if (gstinIdx >= 0) dataCursor.getString(gstinIdx)?.trim() else null,
                    email = if (emailIdx >= 0) dataCursor.getString(emailIdx)?.trim() else null,
                    address = address,
                    state = if (stateIdx >= 0) dataCursor.getString(stateIdx)?.trim() else null,
                    stateCode = null,
                    balance = if (balanceIdx >= 0) try { dataCursor.getDouble(balanceIdx) } catch (_: Exception) { 0.0 } else 0.0,
                    partyType = partyType
                ))
            } catch (e: Exception) { errs.add("Party row: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertParties(parties) }
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

        val nameIdx = columns.indexOfFirst { it.contains("name") || it.contains("item_name") || it.contains("product_name") || it.contains("item") || it.contains("product") }
        if (nameIdx == -1) { return Pair(0, listOf("No name column in $tableName")) }

        val priceIdx = columns.indexOfFirst { it.contains("sale_price") || it.contains("selling_price") || it.contains("selling") || it.contains("price") || it.contains("rate") || it.contains("mrp") }
        val purchasePriceIdx = columns.indexOfFirst { it.contains("purchase_price") || it.contains("cost_price") || it.contains("cost") || it.contains("buying") || it.contains("purchase_rate") }
        val hsnIdx = columns.indexOfFirst { it.contains("hsn") || it.contains("sac") || it.contains("hsn_code") }
        val gstIdx = columns.indexOfFirst { it.contains("tax") || it.contains("gst") || it.contains("tax_rate") || it.contains("gst_rate") }
        val unitIdx = columns.indexOfFirst { it.contains("unit") || it.contains("uom") || it.contains("unit_name") }
        val stockIdx = columns.indexOfFirst { it.contains("stock") || it.contains("quantity") || it.contains("opening_stock") || it.contains("balance_stock") || it.contains("current_stock") }
        val descIdx = columns.indexOfFirst { it.contains("description") || it.contains("desc") || it.contains("details") }
        val skuIdx = columns.indexOfFirst { it.contains("sku") || it.contains("code") || it.contains("item_code") }

        val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
        val items = mutableListOf<ItemEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val name = dataCursor.getString(nameIdx) ?: continue
                if (name.isBlank()) continue
                val gstRate = if (gstIdx >= 0) try {
                    val raw = dataCursor.getString(gstIdx) ?: "0"
                    raw.replace("%", "").trim().toDoubleOrNull() ?: 0.0
                } catch (_: Exception) { 0.0 } else 0.0

                items.add(ItemEntity(
                    companyId = 1L,
                    name = name.trim(),
                    hsnCode = if (hsnIdx >= 0) dataCursor.getString(hsnIdx)?.trim() else null,
                    description = if (descIdx >= 0) dataCursor.getString(descIdx)?.trim() else null,
                    salePrice = if (priceIdx >= 0) try { dataCursor.getDouble(priceIdx) } catch (_: Exception) { 0.0 } else 0.0,
                    purchasePrice = if (purchasePriceIdx >= 0) try { dataCursor.getDouble(purchasePriceIdx) } catch (_: Exception) { 0.0 } else 0.0,
                    gstRate = gstRate,
                    unit = if (unitIdx >= 0) (dataCursor.getString(unitIdx)?.trim() ?: "NOS") else "NOS",
                    stockQuantity = if (stockIdx >= 0) try { dataCursor.getDouble(stockIdx) } catch (_: Exception) { 0.0 } else 0.0,
                    isService = false
                ))
            } catch (e: Exception) { errs.add("Item row: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertItems(items) }
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importInvoicesFromVyaparDb(db: SQLiteDatabase, tableName: String, allTables: List<String>, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM $tableName LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        val numIdx = columns.indexOfFirst { it.contains("number") || it.contains("invoice_number") || it.contains("bill_number") || it.contains("ref") }
        if (numIdx == -1) { return Pair(0, listOf("No invoice number column in $tableName")) }

        val dateIdx = columns.indexOfFirst { it.contains("date") || it.contains("invoice_date") || it.contains("bill_date") || it.contains("created_at") }
        val totalIdx = columns.indexOfFirst { it.contains("total") || it.contains("grand_total") || it.contains("amount") || it.contains("net_amount") || it.contains("bill_amount") }
        val taxIdx = columns.indexOfFirst { it.contains("tax") || it.contains("gst") || it.contains("tax_amount") }
        val cgstIdx = columns.indexOfFirst { it.contains("cgst") || it.contains("central_tax") }
        val sgstIdx = columns.indexOfFirst { it.contains("sgst") || it.contains("state_tax") }
        val igstIdx = columns.indexOfFirst { it.contains("igst") || it.contains("integrated_tax") }
        val discountIdx = columns.indexOfFirst { it.contains("discount") || it.contains("discount_amount") }
        val typeIdx = columns.indexOfFirst { it.contains("type") || it.contains("invoice_type") || it.contains("bill_type") }
        val statusIdx = columns.indexOfFirst { it.contains("status") || it.contains("payment_status") || it.contains("payment_state") }
        val partyIdx = columns.indexOfFirst { it.contains("party") || it.contains("customer") || it.contains("party_name") || it.contains("party_id") }
        val taxableIdx = columns.indexOfFirst { it.contains("taxable") || it.contains("taxable_amount") || it.contains("subtotal") || it.contains("net_total") }

        val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
        val invoices = mutableListOf<InvoiceEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val invNum = dataCursor.getString(numIdx) ?: continue
                if (invNum.isBlank()) continue

                val dateStr = if (dateIdx >= 0) dataCursor.getString(dateIdx) ?: "" else ""
                val dateMillis = try {
                    if (dateStr.contains("-")) {
                        val parts = dateStr.split(" ")[0].split("-")
                        if (parts.size >= 3) {
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse("${parts[0]}-${parts[1]}-${parts[2]}")?.time ?: System.currentTimeMillis()
                        } else System.currentTimeMillis()
                    } else if (dateStr.contains("/")) {
                        val parts = dateStr.split(" ")[0].split("/")
                        if (parts.size >= 3) {
                            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).parse("${parts[0]}/${parts[1]}/${parts[2]}")?.time ?: System.currentTimeMillis()
                        } else System.currentTimeMillis()
                    } else System.currentTimeMillis()
                } catch (_: Exception) { System.currentTimeMillis() }

                val total = if (totalIdx >= 0) try { dataCursor.getDouble(totalIdx) } catch (_: Exception) { 0.0 } else 0.0
                val taxable = if (taxableIdx >= 0) try { dataCursor.getDouble(taxableIdx) } catch (_: Exception) { total } else total
                val cgst = if (cgstIdx >= 0) try { dataCursor.getDouble(cgstIdx) } catch (_: Exception) { 0.0 } else 0.0
                val sgst = if (sgstIdx >= 0) try { dataCursor.getDouble(sgstIdx) } catch (_: Exception) { 0.0 } else 0.0
                val igst = if (igstIdx >= 0) try { dataCursor.getDouble(igstIdx) } catch (_: Exception) { 0.0 } else 0.0
                val tax = if (taxIdx >= 0) try { dataCursor.getDouble(taxIdx) } catch (_: Exception) { cgst + sgst + igst } else cgst + sgst + igst
                val discount = if (discountIdx >= 0) try { dataCursor.getDouble(discountIdx) } catch (_: Exception) { 0.0 } else 0.0

                val invType = if (typeIdx >= 0) {
                    val t = dataCursor.getString(typeIdx) ?: "sale"
                    if (t.contains("purchase") || t.contains("buy")) "purchase" else "sales"
                } else "sales"

                val status = if (statusIdx >= 0) {
                    val s = dataCursor.getString(statusIdx) ?: "unpaid"
                    when {
                        s.contains("paid") || s.contains("complete") -> "paid"
                        s.contains("partial") || s.contains("part") -> "partial"
                        else -> "unpaid"
                    }
                } else "unpaid"

                invoices.add(InvoiceEntity(
                    companyId = 1L,
                    partyId = 0L,
                    invoiceNumber = invNum.trim(),
                    invoiceDate = dateMillis,
                    dueDate = null,
                    subTotal = taxable,
                    discount = discount,
                    taxableAmount = taxable,
                    cgstTotal = if (cgst > 0) cgst else tax / 2,
                    sgstTotal = if (sgst > 0) sgst else tax / 2,
                    igstTotal = igst,
                    totalAmount = total,
                    paymentStatus = status,
                    invoiceType = invType
                ))
            } catch (e: Exception) { errs.add("Invoice row: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertInvoices(invoices) }
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importExpensesFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM $tableName LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        val amountIdx = columns.indexOfFirst { it.contains("amount") || it.contains("expense_amount") || it.contains("total") }
        if (amountIdx == -1) { return Pair(0, listOf("No amount column in $tableName")) }

        val dateIdx = columns.indexOfFirst { it.contains("date") || it.contains("expense_date") || it.contains("created_at") }
        val catIdx = columns.indexOfFirst { it.contains("category") || it.contains("expense_category") || it.contains("type") || it.contains("expense_type") }
        val descIdx = columns.indexOfFirst { it.contains("description") || it.contains("desc") || it.contains("note") || it.contains("detail") }
        val modeIdx = columns.indexOfFirst { it.contains("mode") || it.contains("payment_mode") || it.contains("payment_type") }

        val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
        val expenses = mutableListOf<ExpenseEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val amount = try { dataCursor.getDouble(amountIdx) } catch (_: Exception) { 0.0 }
                if (amount <= 0) continue

                val dateStr = if (dateIdx >= 0) dataCursor.getString(dateIdx) ?: "" else ""
                val dateMillis = try {
                    if (dateStr.contains("-")) {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(dateStr.split(" ")[0])?.time ?: System.currentTimeMillis()
                    } else if (dateStr.contains("/")) {
                        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).parse(dateStr.split(" ")[0])?.time ?: System.currentTimeMillis()
                    } else System.currentTimeMillis()
                } catch (_: Exception) { System.currentTimeMillis() }

                val category = if (catIdx >= 0) dataCursor.getString(catIdx)?.trim() ?: "General" else "General"

                expenses.add(ExpenseEntity(
                    companyId = 1L,
                    category = category,
                    amount = amount,
                    date = dateMillis,
                    description = if (descIdx >= 0) dataCursor.getString(descIdx)?.trim() else null,
                    paymentMode = if (modeIdx >= 0) dataCursor.getString(modeIdx)?.trim() ?: "cash" else "cash"
                ))
            } catch (e: Exception) { errs.add("Expense row: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertExpenses(expenses) }
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

private fun importTransactionsFromVyaparDb(db: SQLiteDatabase, tableName: String, viewModel: ImportViewModel): Pair<Int, List<String>> {
    val errs = mutableListOf<String>()
    try {
        val cursor = db.rawQuery("SELECT * FROM $tableName LIMIT 1", null)
        val columns = cursor.columnNames.map { it.lowercase() }
        cursor.close()

        val amountIdx = columns.indexOfFirst { it.contains("amount") || it.contains("payment_amount") || it.contains("received") }
        if (amountIdx == -1) { return Pair(0, listOf("No amount column in $tableName")) }

        val dateIdx = columns.indexOfFirst { it.contains("date") || it.contains("payment_date") || it.contains("created_at") }
        val typeIdx = columns.indexOfFirst { it.contains("type") || it.contains("payment_type") || it.contains("transaction_type") }
        val modeIdx = columns.indexOfFirst { it.contains("mode") || it.contains("payment_mode") || it.contains("method") }
        val descIdx = columns.indexOfFirst { it.contains("description") || it.contains("note") || it.contains("reference") }

        val dataCursor = db.rawQuery("SELECT * FROM $tableName", null)
        val transactions = mutableListOf<TransactionEntity>()
        while (dataCursor.moveToNext()) {
            try {
                val amount = try { dataCursor.getDouble(amountIdx) } catch (_: Exception) { 0.0 }
                if (amount <= 0) continue

                val dateStr = if (dateIdx >= 0) dataCursor.getString(dateIdx) ?: "" else ""
                val dateMillis = try {
                    if (dateStr.contains("-")) {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(dateStr.split(" ")[0])?.time ?: System.currentTimeMillis()
                    } else if (dateStr.contains("/")) {
                        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).parse(dateStr.split(" ")[0])?.time ?: System.currentTimeMillis()
                    } else System.currentTimeMillis()
                } catch (_: Exception) { System.currentTimeMillis() }

                val type = if (typeIdx >= 0) {
                    val t = dataCursor.getString(typeIdx) ?: "credit"
                    if (t.contains("receive") || t.contains("in") || t.contains("credit")) "credit" else "debit"
                } else "debit"

                transactions.add(TransactionEntity(
                    companyId = 1L,
                    partyId = 0L,
                    amount = amount,
                    type = type,
                    mode = if (modeIdx >= 0) dataCursor.getString(modeIdx)?.trim() ?: "cash" else "cash",
                    description = if (descIdx >= 0) dataCursor.getString(descIdx)?.trim() else null,
                    date = dateMillis
                ))
            } catch (e: Exception) { errs.add("Transaction row: ${e.message}") }
        }
        dataCursor.close()
        val count = kotlinx.coroutines.runBlocking { viewModel.insertTransactions(transactions) }
        return Pair(count, errs)
    } catch (e: Exception) {
        return Pair(0, listOf("Error reading $tableName: ${e.message}"))
    }
}

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
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val partyDao: PartyDao,
    private val itemDao: ItemDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val expenseDao: ExpenseDao,
    private val transactionDao: TransactionDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    suspend fun insertCompany(company: CompanyEntity): Long {
        return try { companyDao.insertCompany(company) } catch (_: Exception) { 0L }
    }

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

    suspend fun insertInvoices(invoices: List<InvoiceEntity>): Int {
        var count = 0
        invoices.forEach { invoice ->
            try { invoiceDao.insertInvoice(invoice); count++ } catch (_: Exception) { }
        }
        return count
    }

    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>): Int {
        var count = 0
        items.forEach { item ->
            try { invoiceItemDao.insertInvoiceItem(item); count++ } catch (_: Exception) { }
        }
        return count
    }

    suspend fun insertExpenses(expenses: List<ExpenseEntity>): Int {
        var count = 0
        expenses.forEach { expense ->
            try { expenseDao.insertExpense(expense); count++ } catch (_: Exception) { }
        }
        return count
    }

    suspend fun insertTransactions(transactions: List<TransactionEntity>): Int {
        var count = 0
        transactions.forEach { txn ->
            try { transactionDao.insertTransaction(txn); count++ } catch (_: Exception) { }
        }
        return count
    }

    fun addParty(name: String, phone: String?, email: String?, gstin: String?, address: String?, state: String?, stateCode: String?, partyType: String, balance: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            partyDao.insertParty(PartyEntity(
                companyId = 1L, name = name, phone = phone, email = email, gstin = gstin,
                address = address, state = state, stateCode = stateCode, balance = balance, partyType = partyType
            ))
        }
    }

    fun addItem(name: String, hsnCode: String?, description: String?, salePrice: Double, purchasePrice: Double, gstRate: Double, unit: String, stockQuantity: Double, isService: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            itemDao.insertItem(ItemEntity(
                companyId = 1L, name = name, hsnCode = hsnCode, description = description,
                salePrice = salePrice, purchasePrice = purchasePrice, gstRate = gstRate,
                unit = unit, stockQuantity = stockQuantity, isService = isService
            ))
        }
    }

    fun addInvoice(partyName: String, invoiceNumber: String, invoiceDate: String, subTotal: Double, discount: Double, taxableAmount: Double, cgst: Double, sgst: Double, igst: Double, total: Double, paid: Double, status: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateMillis = try { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(invoiceDate)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }
            val parties = partyDao.getPartiesByCompany(1L).let { flow ->
                var result: PartyEntity? = null
                val job = kotlinx.coroutines.GlobalScope.launch { flow.collect { list -> result = list.find { it.name == partyName } } }
                job.join()
                result
            }
            invoiceDao.insertInvoice(InvoiceEntity(
                companyId = 1L, partyId = parties?.id ?: 0L, invoiceNumber = invoiceNumber, invoiceDate = dateMillis,
                dueDate = null, subTotal = subTotal, discount = discount, taxableAmount = taxableAmount,
                cgstTotal = cgst, sgstTotal = sgst, igstTotal = igst, totalAmount = total, amountPaid = paid,
                paymentStatus = status, invoiceType = type
            ))
        }
    }

    fun importVyaparTransactions(csvText: String, onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lines = csvText.lines().filter { it.isNotBlank() }
                if (lines.size < 4) { withContext(Dispatchers.Main) { onComplete("Error: Not enough data rows") }; return@launch }

                val dataLines = lines.drop(3)
                val headerLine = lines[2].lowercase()

                var partiesCreated = 0
                var invoicesCreated = 0
                var skippedCancelled = 0
                val partyIdMap = mutableMapOf<String, Long>()

                for (line in dataLines) {
                    try {
                        val cols = parseCsvLine(line)
                        if (cols.size < 8) continue

                        val dateStr = cols[0].trim()
                        val partyName = cols[1].trim().replace("\n", " ")
                        val phone = cols[2].trim().ifBlank { null }?.replace("+91", "")?.trim()
                        val gstin = cols[3].trim().ifBlank { null }
                        val invoiceNo = cols[5].trim()
                        val txnType = cols[6].trim()
                        val totalAmountStr = cols[7].trim().replace(",", "")
                        val paymentType = cols.getOrElse(8) { "" }.trim()
                        val receivedStr = cols.getOrElse(9) { "" }.trim().replace(",", "")
                        val balanceStr = cols.getOrElse(10) { "" }.trim().replace(",", "")

                        if (partyName.isBlank()) continue
                        if (txnType.contains("Cancelled", ignoreCase = true)) { skippedCancelled++; continue }
                        if (txnType.contains("Estimate", ignoreCase = true) || txnType.contains("Quotation", ignoreCase = true)) continue

                        val dateMillis = try {
                            val parts = dateStr.split("/")
                            if (parts.size == 3) {
                                val cal = java.util.Calendar.getInstance()
                                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt(), 0, 0, 0)
                                cal.timeInMillis
                            } else System.currentTimeMillis()
                        } catch (_: Exception) { System.currentTimeMillis() }

                        if (!partyIdMap.containsKey(partyName)) {
                            val stateCode = gstin?.take(2) ?: ""
                            val id = partyDao.insertParty(PartyEntity(
                                companyId = 1L, name = partyName, phone = phone, gstin = gstin,
                                email = null, address = null, state = stateCode, stateCode = stateCode,
                                balance = 0.0, partyType = "customer"
                            ))
                            partyIdMap[partyName] = id
                            partiesCreated++
                        }

                        val partyId = partyIdMap[partyName] ?: 0L
                        val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
                        val received = receivedStr.toDoubleOrNull() ?: 0.0
                        val balance = balanceStr.toDoubleOrNull() ?: 0.0

                        val isPaid = received > 0 && balance <= 0.01
                        val isPartial = received > 0 && balance > 0.01
                        val paymentStatus = when {
                            txnType.contains("Payment-in", ignoreCase = true) -> "paid"
                            isPaid -> "paid"
                            isPartial -> "partial"
                            else -> "unpaid"
                        }

                        invoiceDao.insertInvoice(InvoiceEntity(
                            companyId = 1L, partyId = partyId, invoiceNumber = invoiceNo,
                            invoiceDate = dateMillis, dueDate = null,
                            subTotal = totalAmount, discount = 0.0, taxableAmount = totalAmount,
                            cgstTotal = 0.0, sgstTotal = 0.0, igstTotal = 0.0,
                            totalAmount = totalAmount, amountPaid = received,
                            paymentStatus = paymentStatus, invoiceType = "sales",
                            notes = cols.getOrElse(11) { "" }.trim().ifBlank { null }
                        ))
                        invoicesCreated++
                    } catch (_: Exception) { }
                }

                val summary = buildString {
                    append("Import complete!\n")
                    append("Parties created: $partiesCreated\n")
                    append("Invoices created: $invoicesCreated\n")
                    if (skippedCancelled > 0) append("Cancelled sales skipped: $skippedCancelled")
                }
                withContext(Dispatchers.Main) { onComplete(summary) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete("Error: ${e.message}") }
            }
        }
    }

    fun generateSampleData(onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var count = 0
            val states = listOf(
                "Maharashtra" to "27", "Karnataka" to "29", "Delhi" to "07",
                "Tamil Nadu" to "33", "Gujarat" to "24", "Rajasthan" to "08",
                "Uttar Pradesh" to "09", "West Bengal" to "19", "Telangana" to "36", "Kerala" to "32"
            )

            // STEP 1: Insert all parties first and collect their IDs
            data class PartyData(val name: String, val phone: String, val type: String, val addr: String, val gstin: String, val balance: Double, val stateIdx: Int)

            val partyList = listOf(
                PartyData("Rajesh Traders", "9876543210", "customer", "Mumbai, Maharashtra", "27AABCU9603R1ZM", 25000.0, 0),
                PartyData("Priya Enterprises", "9812345678", "customer", "Bangalore, Karnataka", "29AABCP9876R1ZM", 18500.0, 1),
                PartyData("Amit Hardware", "9988776655", "supplier", "Delhi, NCR", "07AABCA1234R1ZM", -45000.0, 2),
                PartyData("Suresh & Sons", "9765432109", "customer", "Chennai, Tamil Nadu", "33AABCS5678R1ZM", 32000.0, 3),
                PartyData("Vijay Electronics", "9654321098", "customer", "Ahmedabad, Gujarat", "24AABCV9012R1ZM", 12000.0, 4),
                PartyData("Deepak Steel", "9543210987", "supplier", "Jaipur, Rajasthan", "08AABCD3456R1ZM", -67500.0, 5),
                PartyData("Anita Fashion Hub", "9432109876", "customer", "Lucknow, UP", "09AABCF7890R1ZM", 8900.0, 6),
                PartyData("Ganesh Industries", "9321098765", "supplier", "Kolkata, West Bengal", "19AABCG1234R1ZM", -28000.0, 7),
                PartyData("Meera Textiles", "9210987654", "customer", "Hyderabad, Telangana", "36AABCM5678R1ZM", 41000.0, 8),
                PartyData("Kumar Plumbing", "9109876543", "customer", "Kochi, Kerala", "32AABCK9012R1ZM", 15500.0, 9),
                PartyData("Sharma Medical", "9098765432", "supplier", "Pune, Maharashtra", "27AABCS3456R1ZM", -22000.0, 0),
                PartyData("Verma Auto Parts", "8987654321", "customer", "Nagpur, Maharashtra", "27AABCV7890R1ZM", 9500.0, 0),
                PartyData("Joshi Builders", "8876543210", "customer", "Indore, MP", "23AABCJ1234R1ZM", 55000.0, 5),
                PartyData("Reddy Pharmaceuticals", "8765432109", "supplier", "Visakhapatnam, AP", "37AABCR5678R1ZM", -38000.0, 8),
                PartyData("Iyer Software Solutions", "8654321098", "customer", "Coimbatore, TN", "33AABCI9012R1ZM", 72000.0, 3)
            )

            val partyIdMap = mutableMapOf<String, Long>()
            partyList.forEach { pd ->
                val id = partyDao.insertParty(PartyEntity(
                    companyId = 1L, name = pd.name, phone = pd.phone,
                    email = "${pd.name.lowercase().replace(" ", "").replace("&", "and")}@email.com",
                    gstin = pd.gstin, address = pd.addr,
                    state = states[pd.stateIdx].first, stateCode = states[pd.stateIdx].second,
                    balance = pd.balance, partyType = pd.type
                ))
                partyIdMap[pd.name] = id
                count++
            }

            // STEP 2: Insert items
            val itemList = listOf(
                ("Laptop HP 15s", "8471", 45000.0, 38000.0, 18.0, "Pcs", 25.0, false),
                ("Printer Canon G3010", "8443", 13500.0, 11000.0, 18.0, "Pcs", 8.0, false),
                ("Office Chair", "9401", 8500.0, 6500.0, 18.0, "Pcs", 15.0, false),
                ("AC Voltas 1.5 Ton", "8415", 35000.0, 28000.0, 18.0, "Pcs", 5.0, false),
                ("Cement ACC 50kg", "2523", 380.0, 320.0, 28.0, "Bag", 500.0, false),
                ("TMT Bar 12mm", "7214", 55.0, 48.0, 18.0, "Kg", 2000.0, false),
                ("Plywood 19mm", "4412", 85.0, 72.0, 18.0, "Sft", 1500.0, false),
                ("Paint Asian Paints", "3209", 2800.0, 2200.0, 28.0, "Bucket", 30.0, false),
                ("LED Bulb 9W", "9405", 120.0, 85.0, 18.0, "Pcs", 200.0, false),
                ("Pipe SS 1 inch", "7306", 450.0, 380.0, 18.0, "Mtr", 100.0, false),
                ("CPU Intel i5", "8471", 28000.0, 24000.0, 18.0, "Pcs", 12.0, false),
                ("Mouse Logitech", "8471", 450.0, 320.0, 18.0, "Pcs", 50.0, false),
                ("Keyboard Dell", "8471", 1200.0, 900.0, 18.0, "Pcs", 30.0, false),
                ("Monitor LG 24 inch", "8528", 14000.0, 11500.0, 18.0, "Pcs", 10.0, false),
                ("UPS APC 1KVA", "8504", 6500.0, 5200.0, 18.0, "Pcs", 7.0, false),
                ("Cable Cat6", "8544", 3.5, 2.8, 18.0, "Mtr", 5000.0, false),
                ("Switch Socket", "8536", 85.0, 65.0, 18.0, "Pcs", 150.0, false),
                ("Wire 2.5 sq mm", "8544", 18.0, 14.5, 18.0, "Mtr", 3000.0, false),
                ("Consulting Service", "9983", 5000.0, 0.0, 18.0, "Hrs", 0.0, true),
                ("AMC Service", "9987", 12000.0, 0.0, 18.0, "Year", 0.0, true)
            )

            itemList.forEach { (name, hsn, salePrice, purchasePrice, gst, unit, stock, isService) ->
                itemDao.insertItem(ItemEntity(
                    companyId = 1L, name = name, hsnCode = hsn, description = name,
                    salePrice = salePrice, purchasePrice = purchasePrice, gstRate = gst,
                    unit = unit, stockQuantity = stock, isService = isService
                ))
                count++
            }

            // STEP 3: Insert invoices with correct partyId from the map
            data class InvoiceData(val partyName: String, val invNum: String, val date: String, val sub: Double, val disc: Double, val taxable: Double, val cgst: Double, val sgst: Double, val igst: Double, val total: Double, val paid: Double, val status: String, val type: String)

            val invoiceList = listOf(
                InvoiceData("Rajesh Traders", "INV-0001", "2026-06-15", 45000.0, 0.0, 45000.0, 4050.0, 4050.0, 0.0, 53100.0, 53100.0, "paid", "sales"),
                InvoiceData("Priya Enterprises", "INV-0002", "2026-06-20", 13500.0, 0.0, 13500.0, 1215.0, 1215.0, 0.0, 15930.0, 10000.0, "partial", "sales"),
                InvoiceData("Amit Hardware", "PUR-0001", "2026-06-18", 28000.0, 0.0, 28000.0, 2520.0, 2520.0, 0.0, 33040.0, 33040.0, "paid", "purchase"),
                InvoiceData("Suresh & Sons", "INV-0003", "2026-06-22", 8500.0, 500.0, 8000.0, 720.0, 720.0, 0.0, 9440.0, 0.0, "unpaid", "sales"),
                InvoiceData("Vijay Electronics", "INV-0004", "2026-06-25", 35000.0, 0.0, 35000.0, 3150.0, 3150.0, 0.0, 41300.0, 20000.0, "partial", "sales"),
                InvoiceData("Deepak Steel", "PUR-0002", "2026-06-28", 55000.0, 0.0, 55000.0, 4950.0, 4950.0, 0.0, 64900.0, 64900.0, "paid", "purchase"),
                InvoiceData("Anita Fashion Hub", "INV-0005", "2026-07-01", 12000.0, 0.0, 12000.0, 1080.0, 1080.0, 0.0, 14160.0, 14160.0, "paid", "sales"),
                InvoiceData("Ganesh Industries", "PUR-0003", "2026-07-03", 18000.0, 1000.0, 17000.0, 1530.0, 1530.0, 0.0, 20060.0, 10000.0, "partial", "purchase"),
                InvoiceData("Meera Textiles", "INV-0006", "2026-07-05", 22000.0, 0.0, 22000.0, 1980.0, 1980.0, 0.0, 25960.0, 0.0, "unpaid", "sales"),
                InvoiceData("Kumar Plumbing", "INV-0007", "2026-07-08", 8500.0, 0.0, 8500.0, 765.0, 765.0, 0.0, 10030.0, 10030.0, "paid", "sales"),
                InvoiceData("Sharma Medical", "PUR-0004", "2026-07-10", 32000.0, 0.0, 32000.0, 2880.0, 2880.0, 0.0, 37760.0, 37760.0, "paid", "purchase"),
                InvoiceData("Verma Auto Parts", "INV-0008", "2026-07-12", 6500.0, 0.0, 6500.0, 585.0, 585.0, 0.0, 7670.0, 0.0, "unpaid", "sales"),
                InvoiceData("Joshi Builders", "INV-0009", "2026-07-14", 65000.0, 5000.0, 60000.0, 5400.0, 5400.0, 0.0, 70800.0, 40000.0, "partial", "sales"),
                InvoiceData("Reddy Pharmaceuticals", "PUR-0005", "2026-07-15", 42000.0, 0.0, 42000.0, 3780.0, 3780.0, 0.0, 49560.0, 49560.0, "paid", "purchase"),
                InvoiceData("Iyer Software Solutions", "INV-0010", "2026-07-16", 24000.0, 0.0, 24000.0, 2160.0, 2160.0, 0.0, 28320.0, 0.0, "unpaid", "sales")
            )

            invoiceList.forEach { inv ->
                val dateMillis = try { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(inv.date)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }
                val partyId = partyIdMap[inv.partyName] ?: 0L

                invoiceDao.insertInvoice(InvoiceEntity(
                    companyId = 1L, partyId = partyId, invoiceNumber = inv.invNum, invoiceDate = dateMillis,
                    dueDate = dateMillis + 86400000L * 30, subTotal = inv.sub, discount = inv.disc,
                    discountType = "amount", taxableAmount = inv.taxable, cgstTotal = inv.cgst,
                    sgstTotal = inv.sgst, igstTotal = inv.igst, totalAmount = inv.total,
                    amountPaid = inv.paid, paymentStatus = inv.status, invoiceType = inv.type
                ))
                count++
            }

            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
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

                                items.add(
                                    ItemEntity(
                                        companyId = 1L, name = name,
                                        hsnCode = hsnIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        description = name,
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

                                val type = typeIdx.takeIf { it >= 0 }?.let { cols[it] }?.lowercase() ?: "customer"
                                val partyType = when {
                                    type.contains("supplier") || type.contains("vendor") -> "supplier"
                                    type.contains("both") -> "both"
                                    else -> "customer"
                                }

                                parties.add(
                                    PartyEntity(
                                        companyId = 1L, name = name,
                                        phone = phoneIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        gstin = gstinIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        email = emailIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
                                        address = addrIdx.takeIf { it >= 0 }?.let { cols[it] }?.ifBlank { null },
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
                    Button(onClick = { navController.navigate(Screen.VyaparDataImport.route) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) {
                        Icon(Icons.Filled.Storage, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Import Vyapar Backup (.vyb)")
                    }
                }
            }

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

package com.mimo.gstbilling.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.mimo.gstbilling.data.local.entity.*
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelGenerator {

    private val headerStyle: (Workbook) -> HSSFCellStyle = { workbook ->
        (workbook.createCellStyle() as HSSFCellStyle).apply {
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            val font = workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
                fontHeightInPoints = 11
            }
            setFont(font)
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private val currencyFormat: (Workbook) -> HSSFCellStyle = { workbook ->
        (workbook.createCellStyle() as HSSFCellStyle).apply {
            dataFormat = workbook.createDataFormat().getFormat("#,##0.00")
            alignment = HorizontalAlignment.RIGHT
        }
    }

    private val dateStyle: (Workbook) -> HSSFCellStyle = { workbook ->
        (workbook.createCellStyle() as HSSFCellStyle).apply {
            dataFormat = workbook.createDataFormat().getFormat("dd/MM/yyyy")
            alignment = HorizontalAlignment.CENTER
        }
    }

    private val titleStyle: (Workbook) -> HSSFCellStyle = { workbook ->
        (workbook.createCellStyle() as HSSFCellStyle).apply {
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            val font = workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
                fontHeightInPoints = 14
            }
            setFont(font)
        }
    }

    private fun autoSizeColumns(sheet: Sheet, colCount: Int) {
        for (i in 0 until colCount) {
            sheet.setColumnWidth(i, 4000)
        }
    }

    // ========== ITEMS EXPORT ==========
    fun exportItems(context: Context, items: List<ItemEntity>, companyName: String): File {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Items")

        // Title
        val titleRow = sheet.createRow(0)
        titleRow.createCell(0).apply {
            setCellValue("Items Report - $companyName")
            cellStyle = titleStyle(workbook)
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 9))

        // Date
        val dateRow = sheet.createRow(1)
        dateRow.createCell(0).apply { setCellValue("Generated: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())}") }

        // Headers
        val headers = listOf("S.No", "Item Name", "HSN Code", "Sale Price", "Purchase Price", "GST Rate (%)", "Unit", "Stock Qty", "Type", "Description")
        val headerRow = sheet.createRow(3)
        val hStyle = headerStyle(workbook)
        headers.forEachIndexed { i, h ->
            headerRow.createCell(i).apply {
                setCellValue(h)
                cellStyle = hStyle
            }
        }

        // Data
        val cf = currencyFormat(workbook)
        val ds = dateStyle(workbook)
        items.forEachIndexed { index, item ->
            val row = sheet.createRow(4 + index)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(item.name)
            row.createCell(2).setCellValue(item.hsnCode ?: "")
            row.createCell(3).apply { setCellValue(item.salePrice); cellStyle = cf }
            row.createCell(4).apply { setCellValue(item.purchasePrice); cellStyle = cf }
            row.createCell(5).apply { setCellValue(item.gstRate); cellStyle = cf }
            row.createCell(6).setCellValue(item.unit)
            row.createCell(7).apply { setCellValue(item.stockQuantity); cellStyle = cf }
            row.createCell(8).setCellValue(if (item.isService) "Service" else "Product")
            row.createCell(9).setCellValue(item.description ?: "")
        }

        autoSizeColumns(sheet, headers.size)

        val fileName = "Items_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.xls"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    // ========== PARTIES EXPORT ==========
    fun exportParties(context: Context, parties: List<PartyEntity>, companyName: String): File {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Parties")

        val titleRow = sheet.createRow(0)
        titleRow.createCell(0).apply {
            setCellValue("Parties Report - $companyName")
            cellStyle = titleStyle(workbook)
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 8))

        val dateRow = sheet.createRow(1)
        dateRow.createCell(0).apply { setCellValue("Generated: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())}") }

        val headers = listOf("S.No", "Party Name", "Phone", "Email", "GSTIN", "Party Type", "Balance", "Address", "State")
        val headerRow = sheet.createRow(3)
        val hStyle = headerStyle(workbook)
        headers.forEachIndexed { i, h ->
            headerRow.createCell(i).apply {
                setCellValue(h)
                cellStyle = hStyle
            }
        }

        val cf = currencyFormat(workbook)
        parties.forEachIndexed { index, party ->
            val row = sheet.createRow(4 + index)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(party.name)
            row.createCell(2).setCellValue(party.phone ?: "")
            row.createCell(3).setCellValue(party.email ?: "")
            row.createCell(4).setCellValue(party.gstin ?: "")
            row.createCell(5).setCellValue(party.partyType)
            row.createCell(6).apply { setCellValue(party.balance); cellStyle = cf }
            row.createCell(7).setCellValue(party.address ?: "")
            row.createCell(8).setCellValue(party.state ?: "")
        }

        autoSizeColumns(sheet, headers.size)

        val fileName = "Parties_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.xls"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    // ========== INVOICES EXPORT ==========
    fun exportInvoices(context: Context, invoices: List<InvoiceEntity>, companyName: String): File {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Invoices")

        val titleRow = sheet.createRow(0)
        titleRow.createCell(0).apply {
            setCellValue("Invoices Report - $companyName")
            cellStyle = titleStyle(workbook)
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 11))

        val dateRow = sheet.createRow(1)
        dateRow.createCell(0).apply { setCellValue("Generated: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())}") }

        val headers = listOf("S.No", "Invoice No", "Date", "Type", "Party ID", "Subtotal", "Discount", "CGST", "SGST", "IGST", "Total", "Amount Paid", "Balance", "Status", "Notes")
        val headerRow = sheet.createRow(3)
        val hStyle = headerStyle(workbook)
        headers.forEachIndexed { i, h ->
            headerRow.createCell(i).apply {
                setCellValue(h)
                cellStyle = hStyle
            }
        }

        val cf = currencyFormat(workbook)
        val ds = dateStyle(workbook)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        invoices.forEachIndexed { index, invoice ->
            val row = sheet.createRow(4 + index)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(invoice.invoiceNumber)
            row.createCell(2).apply { setCellValue(dateFormat.format(Date(invoice.invoiceDate))); cellStyle = ds }
            row.createCell(3).setCellValue(invoice.invoiceType)
            row.createCell(4).apply { setCellValue(invoice.partyId.toDouble()) }
            row.createCell(5).apply { setCellValue(invoice.subTotal); cellStyle = cf }
            row.createCell(6).apply { setCellValue(invoice.discount); cellStyle = cf }
            row.createCell(7).apply { setCellValue(invoice.cgstTotal); cellStyle = cf }
            row.createCell(8).apply { setCellValue(invoice.sgstTotal); cellStyle = cf }
            row.createCell(9).apply { setCellValue(invoice.igstTotal); cellStyle = cf }
            row.createCell(10).apply { setCellValue(invoice.totalAmount); cellStyle = cf }
            row.createCell(11).apply { setCellValue(invoice.amountPaid); cellStyle = cf }
            row.createCell(12).apply { setCellValue(invoice.totalAmount - invoice.amountPaid); cellStyle = cf }
            row.createCell(13).setCellValue(invoice.paymentStatus)
            row.createCell(14).setCellValue(invoice.notes ?: "")
        }

        autoSizeColumns(sheet, headers.size)

        val fileName = "Invoices_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.xls"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    // ========== EXPENSES EXPORT ==========
    fun exportExpenses(context: Context, expenses: List<ExpenseEntity>, companyName: String): File {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Expenses")

        val titleRow = sheet.createRow(0)
        titleRow.createCell(0).apply {
            setCellValue("Expenses Report - $companyName")
            cellStyle = titleStyle(workbook)
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 6))

        val dateRow = sheet.createRow(1)
        dateRow.createCell(0).apply { setCellValue("Generated: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())}") }

        val headers = listOf("S.No", "Date", "Category", "Amount", "Payment Mode", "Description")
        val headerRow = sheet.createRow(3)
        val hStyle = headerStyle(workbook)
        headers.forEachIndexed { i, h ->
            headerRow.createCell(i).apply {
                setCellValue(h)
                cellStyle = hStyle
            }
        }

        val cf = currencyFormat(workbook)
        val ds = dateStyle(workbook)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        expenses.forEachIndexed { index, expense ->
            val row = sheet.createRow(4 + index)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).apply { setCellValue(dateFormat.format(Date(expense.date))); cellStyle = ds }
            row.createCell(2).setCellValue(expense.category)
            row.createCell(3).apply { setCellValue(expense.amount); cellStyle = cf }
            row.createCell(4).setCellValue(expense.paymentMode)
            row.createCell(5).setCellValue(expense.description ?: "")
        }

        autoSizeColumns(sheet, headers.size)

        val fileName = "Expenses_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.xls"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    // ========== GSTR-1 REPORT EXPORT ==========
    fun exportGstr1(context: Context, invoices: List<InvoiceEntity>, companyName: String): File {
        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("GSTR-1")

        val titleRow = sheet.createRow(0)
        titleRow.createCell(0).apply {
            setCellValue("GSTR-1 Report - $companyName")
            cellStyle = titleStyle(workbook)
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 9))

        val dateRow = sheet.createRow(1)
        dateRow.createCell(0).apply { setCellValue("Period: ${SimpleDateFormat("MMM yyyy", Locale.US).format(Date())}") }

        val headers = listOf("S.No", "Invoice No", "Date", "Party GSTIN", "Invoice Value", "Taxable Value", "CGST", "SGST", "IGST", "Place of Supply")
        val headerRow = sheet.createRow(3)
        val hStyle = headerStyle(workbook)
        headers.forEachIndexed { i, h ->
            headerRow.createCell(i).apply {
                setCellValue(h)
                cellStyle = hStyle
            }
        }

        val cf = currencyFormat(workbook)
        val ds = dateStyle(workbook)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        val salesInvoices = invoices.filter { it.invoiceType == "sales" }
        salesInvoices.forEachIndexed { index, invoice ->
            val row = sheet.createRow(4 + index)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(invoice.invoiceNumber)
            row.createCell(2).apply { setCellValue(dateFormat.format(Date(invoice.invoiceDate))); cellStyle = ds }
            row.createCell(3).setCellValue("") // Party GSTIN would need party lookup
            row.createCell(4).apply { setCellValue(invoice.totalAmount); cellStyle = cf }
            row.createCell(5).apply { setCellValue(invoice.subTotal); cellStyle = cf }
            row.createCell(6).apply { setCellValue(invoice.cgstTotal); cellStyle = cf }
            row.createCell(7).apply { setCellValue(invoice.sgstTotal); cellStyle = cf }
            row.createCell(8).apply { setCellValue(invoice.igstTotal); cellStyle = cf }
            row.createCell(9).setCellValue("") // Place of supply
        }

        // Summary row
        val summaryRow = sheet.createRow(4 + salesInvoices.size + 1)
        summaryRow.createCell(0).apply {
            setCellValue("TOTAL")
            cellStyle = headerStyle(workbook)
        }
        summaryRow.createCell(4).apply {
            setCellValue(salesInvoices.sumOf { it.totalAmount })
            cellStyle = cf
        }
        summaryRow.createCell(5).apply {
            setCellValue(salesInvoices.sumOf { it.subTotal })
            cellStyle = cf
        }
        summaryRow.createCell(6).apply {
            setCellValue(salesInvoices.sumOf { it.cgstTotal })
            cellStyle = cf
        }
        summaryRow.createCell(7).apply {
            setCellValue(salesInvoices.sumOf { it.sgstTotal })
            cellStyle = cf
        }
        summaryRow.createCell(8).apply {
            setCellValue(salesInvoices.sumOf { it.igstTotal })
            cellStyle = cf
        }

        autoSizeColumns(sheet, headers.size)

        val fileName = "GSTR1_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.xls"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        return file
    }

    // ========== SHARING ==========
    fun shareExcel(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Excel File"))
    }

    fun saveExcelToDownloads(context: Context, file: File): Boolean {
        return try {
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MimoGST")
            downloadsDir.mkdirs()
            val destFile = File(downloadsDir, file.name)
            file.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }
}

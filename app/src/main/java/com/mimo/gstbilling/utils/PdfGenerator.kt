package com.mimo.gstbilling.utils

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        company: CompanyEntity?,
        isThermal: Boolean = false
    ): File {
        val pageWidth = if (isThermal) 576 else 595
        val pageHeight = if (isThermal) 2400 else 842

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        val boldPaint = android.graphics.Paint().apply { isFakeBoldText = true }

        var y = 30f
        val leftMargin = if (isThermal) 10f else 40f
        val rightMargin = if (isThermal) (pageWidth - 10f) else (pageWidth - 40f)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        // Company Header
        paint.textSize = if (isThermal) 14f else 18f
        boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", leftMargin, y, boldPaint)
        y += if (isThermal) 18f else 25f

        paint.textSize = if (isThermal) 10f else 12f
        company?.address?.let { canvas.drawText(it, leftMargin, y, paint); y += if (isThermal) 14f else 18f }
        company?.phone?.let { canvas.drawText("Ph: $it", leftMargin, y, paint); y += if (isThermal) 14f else 18f }
        company?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += if (isThermal) 14f else 18f }

        y += 10f
        // Divider
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 15f

        // Invoice Title
        paint.textSize = if (isThermal) 14f else 16f
        boldPaint.textSize = paint.textSize
        canvas.drawText("TAX INVOICE", leftMargin, y, boldPaint)
        y += 20f

        // Invoice details
        paint.textSize = if (isThermal) 10f else 11f
        boldPaint.textSize = paint.textSize
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", leftMargin, y, paint)
        canvas.drawText("Date: ${dateFormat.format(Date(invoice.invoiceDate))}", rightMargin - 150f, y, paint)
        y += 15f

        canvas.drawText("Party ID: ${invoice.partyId}", leftMargin, y, paint)
        canvas.drawText("Status: ${invoice.paymentStatus.uppercase()}", rightMargin - 150f, y, paint)
        y += 20f

        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 12f

        // Items header
        paint.textSize = if (isThermal) 9f else 10f
        boldPaint.textSize = paint.textSize
        val col1 = leftMargin
        val col2 = leftMargin + (if (isThermal) 180f else 220f)
        val col3 = rightMargin - (if (isThermal) 60f else 80f)
        val col4 = rightMargin

        canvas.drawText("Item", col1, y, boldPaint)
        canvas.drawText("Qty", col2, y, boldPaint)
        canvas.drawText("Rate", col2 + 50f, y, boldPaint)
        canvas.drawText("Amount", col4 - 60f, y, boldPaint)
        y += 12f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 8f

        // Items
        paint.textSize = if (isThermal) 9f else 10f
        items.forEach { item ->
            canvas.drawText(item.itemName.take(if (isThermal) 20 else 30), col1, y, paint)
            canvas.drawText("${item.quantity.toInt()}", col2, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.price), col2 + 50f, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.totalAmount), col4 - 60f, y, paint)
            y += 13f
        }

        y += 5f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 12f

        // Totals
        val labelX = rightMargin - 180f
        val valueX = rightMargin

        fun drawLine(label: String, value: String, isBold: Boolean = false) {
            val p = if (isBold) boldPaint else paint
            p.textSize = if (isBold) (if (isThermal) 10f else 12f) else (if (isThermal) 9f else 11f)
            canvas.drawText(label, labelX, y, p)
            canvas.drawText(value, valueX - 60f, y, p)
            y += if (isThermal) 13f else 16f
        }

        drawLine("Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal))
        drawLine("Discount:", String.format(Locale.US, "%.2f", invoice.discount))
        drawLine("CGST:", String.format(Locale.US, "%.2f", invoice.cgstTotal))
        drawLine("SGST:", String.format(Locale.US, "%.2f", invoice.sgstTotal))
        if (invoice.igstTotal > 0) drawLine("IGST:", String.format(Locale.US, "%.2f", invoice.igstTotal))
        if (invoice.tcsAmount > 0) drawLine("TCS (${invoice.tcsRate}%):", String.format(Locale.US, "%.2f", invoice.tcsAmount))
        if (invoice.tdsAmount > 0) drawLine("TDS (${invoice.tdsRate}%):", String.format(Locale.US, "%.2f", invoice.tdsAmount))

        y += 3f
        canvas.drawLine(labelX, y, rightMargin, y, paint)
        y += 15f
        drawLine("TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount), isBold = true)

        y += 15f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 12f

        // Notes
        invoice.notes?.let {
            paint.textSize = if (isThermal) 8f else 9f
            canvas.drawText("Notes: $it", leftMargin, y, paint)
            y += 15f
        }

        // Footer
        paint.textSize = if (isThermal) 8f else 9f
        canvas.drawText("Generated by Mimo GST Billing", leftMargin, y, paint)

        document.finishPage(page)

        val fileName = "Invoice_${invoice.invoiceNumber}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
    }

    fun printPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}

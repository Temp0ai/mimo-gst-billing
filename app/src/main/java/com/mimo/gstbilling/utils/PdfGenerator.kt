package com.mimo.gstbilling.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

class PdfGenerator(private val context: Context) {

    fun generateInvoicePdf(
        invoiceNumber: String,
        companyName: String,
        partyName: String,
        items: List<InvoiceItemPdf>,
        subTotal: Double,
        cgst: Double,
        sgst: Double,
        igst: Double,
        total: Double,
        outputFile: File
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { textSize = 12f; color = android.graphics.Color.BLACK }
        val boldPaint = Paint(paint).apply { typeface = Typeface.DEFAULT_BOLD }
        val titlePaint = Paint(boldPaint).apply { textSize = 18f }

        // Header
        canvas.drawText(companyName, 50f, 50f, titlePaint)
        canvas.drawText("GST INVOICE", 450f, 50f, titlePaint)
        canvas.drawText("Invoice #: $invoiceNumber", 50f, 80f, paint)
        canvas.drawText("Bill To: $partyName", 50f, 100f, paint)

        // Items Table Header
        var y = 140f
        canvas.drawLine(50f, y - 10f, 545f, y - 10f, paint)
        canvas.drawText("Item", 60f, y, boldPaint)
        canvas.drawText("Qty", 300f, y, boldPaint)
        canvas.drawText("Price", 360f, y, boldPaint)
        canvas.drawText("GST%", 420f, y, boldPaint)
        canvas.drawText("Amount", 480f, y, boldPaint)
        y += 20f
        canvas.drawLine(50f, y - 5f, 545f, y - 5f, paint)

        // Items
        items.forEach { item ->
            canvas.drawText(item.name, 60f, y, paint)
            canvas.drawText(item.qty.toString(), 300f, y, paint)
            canvas.drawText(String.format("%.2f", item.price), 360f, y, paint)
            canvas.drawText(item.gstRate.toString(), 420f, y, paint)
            canvas.drawText(String.format("%.2f", item.amount), 480f, y, paint)
            y += 20f
        }

        // Totals
        y += 20f
        canvas.drawLine(50f, y - 10f, 545f, y - 10f, paint)
        canvas.drawText("Sub Total:", 400f, y, boldPaint)
        canvas.drawText(String.format("%.2f", subTotal), 480f, y, boldPaint)
        y += 20f
        canvas.drawText("CGST:", 400f, y, paint)
        canvas.drawText(String.format("%.2f", cgst), 480f, y, paint)
        y += 20f
        canvas.drawText("SGST:", 400f, y, paint)
        canvas.drawText(String.format("%.2f", sgst), 480f, y, paint)
        y += 20f
        canvas.drawText("IGST:", 400f, y, paint)
        canvas.drawText(String.format("%.2f", igst), 480f, y, paint)
        y += 30f
        canvas.drawText("TOTAL:", 400f, y, titlePaint)
        canvas.drawText(String.format("%.2f", total), 480f, y, titlePaint)

        document.finishPage(page)

        FileOutputStream(outputFile).use { output ->
            document.writeTo(output)
        }
        document.close()
    }
}

data class InvoiceItemPdf(
    val name: String,
    val qty: Double,
    val price: Double,
    val gstRate: Double,
    val amount: Double
)

package com.mimo.gstbilling.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
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
        val pageHeight = if (isThermal) 3200 else 842

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        val boldPaint = android.graphics.Paint().apply { isFakeBoldText = true }
        val lightPaint = android.graphics.Paint().apply { color = android.graphics.Color.GRAY; textSize = 9f }

        var y = 30f
        val leftMargin = if (isThermal) 10f else 40f
        val rightMargin = if (isThermal) (pageWidth - 10f) else (pageWidth - 40f)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        // Company Header
        paint.textSize = if (isThermal) 14f else 20f
        boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", leftMargin, y, boldPaint)
        y += if (isThermal) 18f else 28f

        paint.textSize = if (isThermal) 10f else 11f
        company?.address?.let { canvas.drawText(it, leftMargin, y, paint); y += if (isThermal) 14f else 16f }
        company?.phone?.let { canvas.drawText("Ph: $it", leftMargin, y, paint); y += if (isThermal) 14f else 16f }
        company?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += if (isThermal) 14f else 16f }

        y += 8f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 12f

        // Invoice Title
        val title = when (company?.let { true }) {
            else -> "TAX INVOICE"
        }
        paint.textSize = if (isThermal) 14f else 16f
        boldPaint.textSize = paint.textSize
        canvas.drawText(title, leftMargin, y, boldPaint)
        y += 22f

        // Invoice details
        paint.textSize = if (isThermal) 10f else 11f
        boldPaint.textSize = paint.textSize
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", leftMargin, y, paint)
        canvas.drawText("Date: ${dateFormat.format(Date(invoice.invoiceDate))}", rightMargin - 160f, y, paint)
        y += 16f
        canvas.drawText("Party ID: ${invoice.partyId}", leftMargin, y, paint)
        canvas.drawText("Payment: ${invoice.paymentStatus.uppercase()}", rightMargin - 160f, y, paint)
        y += 20f

        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 12f

        // Items header
        paint.textSize = if (isThermal) 9f else 10f
        boldPaint.textSize = paint.textSize
        val col1 = leftMargin
        val col2 = leftMargin + (if (isThermal) 180f else 200f)
        val col3 = col2 + 50f
        val col4 = rightMargin

        canvas.drawText("Item", col1, y, boldPaint)
        canvas.drawText("Qty", col2, y, boldPaint)
        canvas.drawText("Rate", col3, y, boldPaint)
        canvas.drawText("Amount", col4 - 65f, y, boldPaint)
        y += 12f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 8f

        // Items
        paint.textSize = if (isThermal) 9f else 10f
        items.forEach { item ->
            canvas.drawText(item.itemName.take(if (isThermal) 20 else 28), col1, y, paint)
            canvas.drawText("${item.quantity.toInt()} ${item.unit}", col2, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.price), col3, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.totalAmount), col4 - 65f, y, paint)

            paint.textSize = 8f
            lightPaint.textSize = 8f
            val gstText = "GST ${item.gstRate.toInt()}%"
            canvas.drawText(gstText, col1, y + 10f, lightPaint)
            paint.textSize = if (isThermal) 9f else 10f
            y += 16f
        }

        y += 5f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 12f

        // Totals
        val labelX = rightMargin - 180f
        val valueX = rightMargin - 10f

        fun drawLine(label: String, value: String, isBold: Boolean = false) {
            val p = if (isBold) boldPaint else paint
            p.textSize = if (isBold) (if (isThermal) 10f else 12f) else (if (isThermal) 9f else 11f)
            canvas.drawText(label, labelX, y, p)
            canvas.drawText(value, valueX - 60f, y, p)
            y += if (isThermal) 13f else 16f
        }

        drawLine("Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal))
        if (invoice.discount > 0) drawLine("Discount:", String.format(Locale.US, "-%.2f", invoice.discount))
        drawLine("CGST:", String.format(Locale.US, "%.2f", invoice.cgstTotal))
        drawLine("SGST:", String.format(Locale.US, "%.2f", invoice.sgstTotal))
        if (invoice.igstTotal > 0) drawLine("IGST:", String.format(Locale.US, "%.2f", invoice.igstTotal))
        if (invoice.tcsAmount > 0) drawLine("TCS (${invoice.tcsRate}%):", String.format(Locale.US, "%.2f", invoice.tcsAmount))
        if (invoice.tdsAmount > 0) drawLine("TDS (${invoice.tdsRate}%):", String.format(Locale.US, "%.2f", invoice.tdsAmount))
        if (invoice.roundOff != 0.0) drawLine("Round Off:", String.format(Locale.US, "%.2f", invoice.roundOff))

        y += 3f
        canvas.drawLine(labelX, y, rightMargin, y, paint)
        y += 18f
        drawLine("TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount), isBold = true)
        y += 5f

        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 15f

        // Bank Details Section
        val hasBankDetails = !company?.bankName.isNullOrBlank() || !company?.bankAccountNumber.isNullOrBlank()
        if (hasBankDetails) {
            paint.textSize = if (isThermal) 11f else 13f
            boldPaint.textSize = paint.textSize
            canvas.drawText("Bank Details", leftMargin, y, boldPaint)
            y += 16f

            paint.textSize = if (isThermal) 9f else 10f
            company?.bankName?.let { canvas.drawText("Bank: $it", leftMargin, y, paint); y += 14f }
            company?.bankBranch?.let { canvas.drawText("Branch: $it", leftMargin, y, paint); y += 14f }
            company?.bankAccountNumber?.let { canvas.drawText("A/C No: $it", leftMargin, y, paint); y += 14f }
            company?.bankIfsc?.let { canvas.drawText("IFSC: $it", leftMargin, y, paint); y += 14f }
            company?.bankUpiId?.let { canvas.drawText("UPI: $it", leftMargin, y, paint); y += 14f }
            y += 8f
        }

        // QR Code for UPI Payment
        val upiId = company?.bankUpiId
        if (!upiId.isNullOrBlank() && !isThermal) {
            try {
                val upiString = "upi://pay?pa=$upiId&pn=${company?.name ?: "Business"}&am=${String.format(Locale.US, "%.2f", invoice.totalAmount)}&cu=INR&tn=Payment for ${invoice.invoiceNumber}"
                val qrBitmap = generateQrCode(upiString, 120)
                val qrLeft = rightMargin - 140f
                canvas.drawBitmap(qrBitmap, null, android.graphics.RectF(qrLeft, y, qrLeft + 120f, y + 120f))
                paint.textSize = 8f
                canvas.drawText("Scan to Pay", qrLeft + 30f, y + 130f, paint)
                paint.textSize = if (isThermal) 9f else 10f
            } catch (e: Exception) {
                // QR generation failed, skip it
            }
        }

        // Terms & Conditions
        company?.termsAndConditions?.let {
            if (hasBankDetails || !upiId.isNullOrBlank()) {
                y += if (upiId.isNullOrBlank() || isThermal) 10f else 135f
            } else {
                y += 10f
            }
            paint.textSize = if (isThermal) 7f else 8f
            boldPaint.textSize = paint.textSize
            canvas.drawText("Terms & Conditions:", leftMargin, y, boldPaint)
            y += 12f
            paint.textSize = if (isThermal) 7f else 8f
            canvas.drawText(it.take(if (isThermal) 80 else 100), leftMargin, y, paint)
            y += 14f
        }

        // Notes
        invoice.notes?.let {
            y += 4f
            paint.textSize = if (isThermal) 8f else 9f
            canvas.drawText("Notes: $it", leftMargin, y, paint)
            y += 14f
        }

        // Footer
        y += 10f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 14f
        paint.textSize = if (isThermal) 8f else 9f
        lightPaint.textSize = paint.textSize
        canvas.drawText("Powered by Mimo GST Billing", leftMargin, y, lightPaint)

        document.finishPage(page)

        val fileName = "Invoice_${invoice.invoiceNumber}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    private fun generateQrCode(content: String, size: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
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

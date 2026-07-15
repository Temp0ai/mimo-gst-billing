package com.mimo.gstbilling.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class InvoiceStyle(val displayName: String, val description: String) {
    CLASSIC("Classic", "Clean traditional layout with blue accents"),
    MODERN("Modern", "Minimalist design with bold headers"),
    ELEGANT("Elegant", "Sophisticated design with subtle borders"),
    PROFESSIONAL("Professional", "Corporate style with structured grid"),
    BOLD("Bold", "Strong colors and large typography"),
    COMPACT("Compact", "Fits more items on one page"),
    MINIMAL("Minimal", "Simple clean design, no borders"),
    DETAILED("Detailed", "Full breakdown with all fields visible")
}

object PdfTemplateRenderer {

    fun renderPdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        company: CompanyEntity?,
        style: InvoiceStyle = InvoiceStyle.CLASSIC,
        isThermal: Boolean = false
    ): PdfDocument {
        val pageWidth = if (isThermal) 576 else 595
        val pageHeight = if (isThermal) 3200 else 842
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val boldPaint = Paint().apply { isFakeBoldText = true }
        val lightPaint = Paint().apply { color = Color.GRAY; textSize = 9f }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        var y = 30f
        val leftMargin = if (isThermal) 10f else 40f
        val rightMargin = if (isThermal) (pageWidth - 10f) else (pageWidth - 40f)

        when (style) {
            InvoiceStyle.CLASSIC -> drawClassic(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
            InvoiceStyle.MODERN -> drawModern(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
            InvoiceStyle.ELEGANT -> drawElegant(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
            InvoiceStyle.PROFESSIONAL -> drawProfessional(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
            InvoiceStyle.BOLD -> drawBold(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
            InvoiceStyle.COMPACT -> drawCompact(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
            InvoiceStyle.MINIMAL -> drawMinimal(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
            InvoiceStyle.DETAILED -> drawDetailed(canvas, paint, boldPaint, lightPaint, context, company, invoice, items, leftMargin, rightMargin, y, isThermal, dateFormat)
        }

        document.finishPage(page)
        return document
    }

    private fun drawLogo(canvas: Canvas, context: Context, company: CompanyEntity?, x: Float, y: Float, size: Float): Float {
        company?.logoUri?.let { uriStr ->
            try {
                val uri = android.net.Uri.parse(uriStr)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val scaled = Bitmap.createScaledBitmap(bitmap, size.toInt(), size.toInt(), true)
                    canvas.drawBitmap(scaled, x, y, null)
                    scaled.recycle()
                    return size
                }
            } catch (_: Exception) {}
        }
        return 0f
    }

    private fun drawClassic(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        // Blue header line
        paint.color = Color.parseColor("#0075E8")
        canvas.drawRect(leftMargin, y - 15f, rightMargin, y - 5f, paint)
        paint.color = Color.BLACK

        val logoSize = drawLogo(canvas, context, company, leftMargin, y - 45f, 40f)
        val textX = if (logoSize > 0) leftMargin + logoSize + 10f else leftMargin

        paint.textSize = if (isThermal) 14f else 18f; boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", textX, y, boldPaint)
        y += if (isThermal) 18f else 24f
        paint.textSize = if (isThermal) 9f else 10f
        company?.address?.let { canvas.drawText(it, textX, y, paint); y += 14f }
        company?.phone?.let { canvas.drawText("Ph: $it", textX, y, paint); y += 14f }
        company?.gstin?.let { canvas.drawText("GSTIN: $it", textX, y, paint); y += 14f }

        y += 8f
        paint.color = Color.parseColor("#0075E8")
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        paint.color = Color.BLACK
        y += 14f

        paint.textSize = if (isThermal) 12f else 14f; boldPaint.textSize = paint.textSize
        canvas.drawText("TAX INVOICE", leftMargin, y, boldPaint)
        y += 20f

        paint.textSize = if (isThermal) 9f else 10f
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", leftMargin, y, paint)
        canvas.drawText("Date: ${dateFormat.format(Date(invoice.invoiceDate))}", rightMargin - 160f, y, paint)
        y += 14f
        canvas.drawText("Party ID: ${invoice.partyId}", leftMargin, y, paint)
        canvas.drawText("Payment: ${invoice.paymentStatus.uppercase()}", rightMargin - 160f, y, paint)
        y += 18f

        paint.color = Color.parseColor("#0075E8")
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        paint.color = Color.BLACK
        y += 12f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.CLASSIC)
        y += (items.size * 18f) + 30f

        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.CLASSIC)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawModern(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        // Dark header band
        paint.color = Color.parseColor("#1A237E")
        canvas.drawRect(0f, 0f, rightMargin + 40f, 80f, paint)
        paint.color = Color.WHITE
        paint.textSize = if (isThermal) 14f else 22f; boldPaint.textSize = paint.textSize
        drawLogo(canvas, context, company, leftMargin, 15f, 45f)
        canvas.drawText(company?.name ?: "My Business", leftMargin + 55f, 40f, boldPaint)
        paint.textSize = if (isThermal) 9f else 11f
        company?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin + 55f, 58f, paint) }

        paint.color = Color.BLACK
        y = 100f
        paint.textSize = if (isThermal) 12f else 14f; boldPaint.textSize = paint.textSize
        canvas.drawText("INVOICE", leftMargin, y, boldPaint)
        y += 22f
        paint.textSize = if (isThermal) 9f else 10f
        canvas.drawText("${invoice.invoiceNumber}  |  ${dateFormat.format(Date(invoice.invoiceDate))}  |  ${invoice.paymentStatus.uppercase()}", leftMargin, y, paint)
        y += 20f

        canvas.drawRect(leftMargin, y, rightMargin, y + 2f, paint)
        y += 14f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.MODERN)
        y += (items.size * 18f) + 30f

        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.MODERN)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawElegant(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        // Double border header
        paint.color = Color.parseColor("#455A64")
        canvas.drawRect(leftMargin, y - 20f, rightMargin, y - 18f, paint)
        canvas.drawRect(leftMargin, y - 16f, rightMargin, y - 14f, paint)

        val logoSize = drawLogo(canvas, context, company, leftMargin, y - 50f, 40f)
        val textX = if (logoSize > 0) leftMargin + logoSize + 10f else leftMargin

        paint.textSize = if (isThermal) 14f else 18f; boldPaint.textSize = paint.textSize
        paint.color = Color.parseColor("#37474F")
        canvas.drawText(company?.name ?: "My Business", textX, y, boldPaint)
        paint.color = Color.BLACK
        y += if (isThermal) 18f else 24f
        paint.textSize = if (isThermal) 9f else 10f
        company?.address?.let { canvas.drawText(it, textX, y, paint); y += 14f }
        company?.gstin?.let { canvas.drawText("GSTIN: $it", textX, y, paint); y += 14f }

        y += 8f
        paint.color = Color.parseColor("#455A64")
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        canvas.drawLine(leftMargin, y + 2f, rightMargin, y + 2f, paint)
        paint.color = Color.BLACK
        y += 16f

        paint.textSize = if (isThermal) 12f else 14f; boldPaint.textSize = paint.textSize
        canvas.drawText("Invoice", leftMargin, y, boldPaint)
        y += 20f

        paint.textSize = if (isThermal) 9f else 10f
        canvas.drawText("${invoice.invoiceNumber}   ${dateFormat.format(Date(invoice.invoiceDate))}", leftMargin, y, paint)
        y += 18f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.ELEGANT)
        y += (items.size * 18f) + 30f
        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.ELEGANT)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawProfessional(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        paint.color = Color.parseColor("#0D47A1")
        canvas.drawRect(leftMargin, y - 20f, rightMargin, y, paint)

        val logoSize = drawLogo(canvas, context, company, leftMargin + 5f, y - 18f, 16f)
        paint.color = Color.WHITE; paint.textSize = if (isThermal) 12f else 16f; boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", leftMargin + 24f, y - 5f, boldPaint)
        paint.color = Color.BLACK; y += 16f

        paint.textSize = if (isThermal) 9f else 10f
        company?.address?.let { canvas.drawText(it, leftMargin, y, paint); y += 13f }
        company?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += 13f }

        y += 6f
        paint.color = Color.parseColor("#0D47A1")
        canvas.drawRect(leftMargin, y, rightMargin, y + 1f, paint)
        paint.color = Color.BLACK; y += 14f

        paint.textSize = if (isThermal) 11f else 13f; boldPaint.textSize = paint.textSize
        canvas.drawText("TAX INVOICE  #${invoice.invoiceNumber}", leftMargin, y, boldPaint)
        paint.textSize = if (isThermal) 9f else 10f
        canvas.drawText(dateFormat.format(Date(invoice.invoiceDate)), rightMargin - 80f, y, paint)
        y += 18f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.PROFESSIONAL)
        y += (items.size * 18f) + 30f
        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.PROFESSIONAL)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawBold(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        paint.color = Color.parseColor("#D32F2F")
        canvas.drawRect(0f, 0f, rightMargin + 40f, 90f, paint)

        val logoSize = drawLogo(canvas, context, company, leftMargin, 15f, 50f)
        val textX = if (logoSize > 0) leftMargin + logoSize + 10f else leftMargin
        paint.color = Color.WHITE; paint.textSize = if (isThermal) 16f else 24f; boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", textX, 45f, boldPaint)
        paint.textSize = if (isThermal) 9f else 11f
        company?.gstin?.let { canvas.drawText("GSTIN: $it", textX, 65f, paint) }

        paint.color = Color.BLACK; y = 105f
        paint.textSize = if (isThermal) 14f else 18f; boldPaint.textSize = paint.textSize
        paint.color = Color.parseColor("#D32F2F")
        canvas.drawText("INVOICE", leftMargin, y, boldPaint)
        paint.color = Color.BLACK; y += 22f

        paint.textSize = if (isThermal) 9f else 10f
        canvas.drawText("${invoice.invoiceNumber}  |  ${dateFormat.format(Date(invoice.invoiceDate))}  |  ${invoice.paymentStatus.uppercase()}", leftMargin, y, paint)
        y += 18f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.BOLD)
        y += (items.size * 18f) + 30f
        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.BOLD)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawCompact(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        paint.textSize = if (isThermal) 12f else 16f; boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", leftMargin, y, boldPaint)
        y += if (isThermal) 16f else 20f
        paint.textSize = if (isThermal) 8f else 9f
        company?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += 12f }
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 8f

        paint.textSize = if (isThermal) 9f else 10f
        canvas.drawText("#${invoice.invoiceNumber}  ${dateFormat.format(Date(invoice.invoiceDate))}  ${invoice.paymentStatus.uppercase()}", leftMargin, y, paint)
        y += 14f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.COMPACT)
        y += (items.size * 14f) + 20f
        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.COMPACT)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawMinimal(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        paint.textSize = if (isThermal) 14f else 18f; boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", leftMargin, y, boldPaint)
        y += if (isThermal) 18f else 24f
        paint.textSize = if (isThermal) 9f else 10f
        company?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += 14f }
        y += 4f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 10f

        paint.textSize = if (isThermal) 10f else 12f
        canvas.drawText("Invoice ${invoice.invoiceNumber}  •  ${dateFormat.format(Date(invoice.invoiceDate))}", leftMargin, y, paint)
        y += 18f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.MINIMAL)
        y += (items.size * 16f) + 25f
        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.MINIMAL)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawDetailed(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, dateFormat: SimpleDateFormat) {
        var y = startY
        paint.color = Color.parseColor("#1B5E20")
        canvas.drawRect(leftMargin, y - 20f, rightMargin, y, paint)

        val logoSize = drawLogo(canvas, context, company, leftMargin + 5f, y - 18f, 16f)
        paint.color = Color.WHITE; paint.textSize = if (isThermal) 12f else 16f; boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", leftMargin + 24f, y - 5f, boldPaint)
        paint.color = Color.BLACK; y += 16f

        paint.textSize = if (isThermal) 9f else 10f
        company?.address?.let { canvas.drawText(it, leftMargin, y, paint); y += 13f }
        company?.phone?.let { canvas.drawText("Ph: $it", leftMargin, y, paint); y += 13f }
        company?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += 13f }
        company?.msmeUdyamNumber?.let { canvas.drawText("MSME: $it", leftMargin, y, paint); y += 13f }

        y += 6f
        paint.color = Color.parseColor("#1B5E20")
        canvas.drawRect(leftMargin, y, rightMargin, y + 1f, paint)
        paint.color = Color.BLACK; y += 14f

        paint.textSize = if (isThermal) 11f else 13f; boldPaint.textSize = paint.textSize
        canvas.drawText("TAX INVOICE  #${invoice.invoiceNumber}", leftMargin, y, boldPaint)
        paint.textSize = if (isThermal) 9f else 10f
        canvas.drawText("${dateFormat.format(Date(invoice.invoiceDate))}  |  ${invoice.paymentStatus.uppercase()}", rightMargin - 140f, y, paint)
        y += 18f

        drawItemsTable(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, InvoiceStyle.DETAILED)
        y += (items.size * 18f) + 30f
        drawTotals(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal, InvoiceStyle.DETAILED)
        y += 100f

        drawBankDetails(canvas, paint, boldPaint, context, company, invoice, leftMargin, y, isThermal)
        y += 80f
        drawDeclaration(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)
    }

    private fun drawItemsTable(canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint, items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, style: InvoiceStyle) {
        var y = startY
        val col1 = leftMargin
        val col2 = leftMargin + (if (isThermal) 180f else 200f)
        val col3 = col2 + 50f
        val col4 = rightMargin

        val headerColor = when (style) {
            InvoiceStyle.CLASSIC -> Color.parseColor("#0075E8")
            InvoiceStyle.BOLD -> Color.parseColor("#D32F2F")
            InvoiceStyle.MODERN -> Color.parseColor("#1A237E")
            InvoiceStyle.ELEGANT -> Color.parseColor("#455A64")
            InvoiceStyle.PROFESSIONAL -> Color.parseColor("#0D47A1")
            InvoiceStyle.DETAILED -> Color.parseColor("#1B5E20")
            else -> Color.DKGRAY
        }

        if (style != InvoiceStyle.MINIMAL && style != InvoiceStyle.COMPACT) {
            paint.color = headerColor
            canvas.drawRect(leftMargin, y - 12f, rightMargin, y + 2f, paint)
            paint.color = Color.WHITE
        }

        paint.textSize = if (isThermal) 9f else 10f; boldPaint.textSize = paint.textSize
        canvas.drawText("Item", col1 + 4f, y, boldPaint)
        canvas.drawText("Qty", col2, y, boldPaint)
        canvas.drawText("Rate", col3, y, boldPaint)
        canvas.drawText("Amount", col4 - 65f, y, boldPaint)
        paint.color = Color.BLACK
        y += 14f

        if (style != InvoiceStyle.MINIMAL) {
            canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        }
        y += 6f

        paint.textSize = if (isThermal) 9f else 10f
        items.forEach { item ->
            canvas.drawText(item.itemName.take(if (isThermal) 20 else 28), col1, y, paint)
            canvas.drawText("${item.quantity.toInt()} ${item.unit}", col2, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.price), col3, y, paint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.totalAmount), col4 - 65f, y, paint)
            paint.textSize = 8f; lightPaint.textSize = 8f
            canvas.drawText("GST ${item.gstRate.toInt()}%", col1, y + 10f, lightPaint)
            paint.textSize = if (isThermal) 9f else 10f
            y += if (style == InvoiceStyle.COMPACT) 14f else 18f
        }

        y += 5f
        if (style != InvoiceStyle.MINIMAL) {
            canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        }
    }

    private fun drawTotals(canvas: Canvas, paint: Paint, boldPaint: Paint, invoice: InvoiceEntity, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean, style: InvoiceStyle) {
        var y = startY
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
        val totalColor = when (style) {
            InvoiceStyle.CLASSIC -> Color.parseColor("#0075E8")
            InvoiceStyle.BOLD -> Color.parseColor("#D32F2F")
            InvoiceStyle.MODERN -> Color.parseColor("#1A237E")
            else -> Color.BLACK
        }
        paint.color = totalColor
        canvas.drawLine(labelX, y, rightMargin, y, paint)
        paint.color = Color.BLACK
        y += 18f
        drawLine("TOTAL:", String.format(Locale.US, "%.2f", invoice.totalAmount), isBold = true)
    }

    private fun drawBankDetails(canvas: Canvas, paint: Paint, boldPaint: Paint, context: Context, company: CompanyEntity?, invoice: InvoiceEntity, leftMargin: Float, startY: Float, isThermal: Boolean) {
        val hasBankDetails = !company?.bankName.isNullOrBlank() || !company?.bankAccountNumber.isNullOrBlank()
        if (!hasBankDetails) return
        var y = startY

        // Draw bank logo
        val bankLogoSize = if (isThermal) 20f else 28f
        val bankLogoResId = BankLogoMapper.getBankLogo(company?.bankName)
        try {
            val bankLogoDrawable = androidx.core.content.ContextCompat.getDrawable(context, bankLogoResId)
            if (bankLogoDrawable != null) {
                val bitmap = Bitmap.createBitmap(bankLogoSize.toInt(), bankLogoSize.toInt(), Bitmap.Config.ARGB_8888)
                val logoCanvas = Canvas(bitmap)
                bankLogoDrawable.setBounds(0, 0, bankLogoSize.toInt(), bankLogoSize.toInt())
                bankLogoDrawable.draw(logoCanvas)
                canvas.drawBitmap(bitmap, leftMargin, y - bankLogoSize + 2f, null)
                bitmap.recycle()
            }
        } catch (_: Exception) {}

        paint.textSize = if (isThermal) 10f else 12f; boldPaint.textSize = paint.textSize
        canvas.drawText("Bank Details", leftMargin + bankLogoSize + 6f, y, boldPaint)
        y += 16f
        paint.textSize = if (isThermal) 9f else 10f
        company?.bankName?.let { canvas.drawText("Bank: $it", leftMargin, y, paint); y += 14f }
        company?.bankBranch?.let { canvas.drawText("Branch: $it", leftMargin, y, paint); y += 14f }
        company?.bankAccountNumber?.let { canvas.drawText("A/C No: $it", leftMargin, y, paint); y += 14f }
        company?.bankIfsc?.let { canvas.drawText("IFSC: $it", leftMargin, y, paint); y += 14f }
        company?.bankUpiId?.let { canvas.drawText("UPI: $it", leftMargin, y, paint); y += 14f }
        y += 6f

        // Draw payment app logos
        val upiId = company?.bankUpiId
        if (!upiId.isNullOrBlank() && !isThermal) {
            val paymentLogos = listOf(
                R.drawable.ic_paytm to "Paytm",
                R.drawable.ic_phonepe to "PhonePe",
                R.drawable.ic_gpay to "GPay",
                R.drawable.ic_bhim to "BHIM"
            )
            val logoSize = 14f
            val spacing = 4f
            var logoX = leftMargin
            val logoY = y

            paymentLogos.forEach { (resId, name) ->
                try {
                    val drawable = androidx.core.content.ContextCompat.getDrawable(context, resId)
                    if (drawable != null) {
                        val bitmap = Bitmap.createBitmap(logoSize.toInt(), logoSize.toInt(), Bitmap.Config.ARGB_8888)
                        val logoCanvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, logoSize.toInt(), logoSize.toInt())
                        drawable.draw(logoCanvas)
                        canvas.drawBitmap(bitmap, logoX, logoY, null)
                        bitmap.recycle()
                    }
                } catch (_: Exception) {}
                logoX += logoSize + spacing
            }
        }
    }

    private fun drawDeclaration(canvas: Canvas, paint: Paint, boldPaint: Paint, company: CompanyEntity?, leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean) {
        val declarationText = company?.declaration
        if (declarationText.isNullOrBlank()) return
        var y = startY
        paint.textSize = if (isThermal) 9f else 11f; boldPaint.textSize = paint.textSize
        canvas.drawText("Terms and Conditions:", leftMargin, y, boldPaint)
        y += 16f
        paint.textSize = if (isThermal) 7f else 9f
        declarationText.split("\n").filter { it.isNotBlank() }.forEachIndexed { index, line ->
            canvas.drawText("${index + 1}. ${line.trim()}", leftMargin + 8f, y, paint)
            y += 13f
        }
    }
}

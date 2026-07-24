package com.mimo.gstbilling.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.mimo.gstbilling.R
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class PdfCopyType(val displayName: String) {
    ORIGINAL("Original for Recipient"),
    DUPLICATE("Duplicate for Transporter"),
    TRIPLICATE("Duplicate for Supplier")
}

enum class WatermarkType(val displayName: String) {
    NONE(""),
    PAID("PAID"),
    UNPAID("UNPAID"),
    DRAFT("DRAFT"),
    CANCELLED("CANCELLED"),
    OVERDUE("OVERDUE"),
    PARTIAL("PARTIALLY PAID")
}

object PdfGenerator {

    fun generateInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        company: CompanyEntity?,
        party: PartyEntity? = null,
        isThermal: Boolean = false,
        templateStyle: String? = null,
        copyType: PdfCopyType = PdfCopyType.ORIGINAL,
        watermark: WatermarkType = WatermarkType.NONE
    ): File {
        val pageWidth = if (isThermal) 576 else 595
        val pageHeight = if (isThermal) 3200 else 842
        val leftMargin = if (isThermal) 10f else 40f
        val rightMargin = if (isThermal) (pageWidth - 10f) else (pageWidth - 40f)
        val contentWidth = rightMargin - leftMargin

        val paint = Paint().apply { isAntiAlias = true }
        val boldPaint = Paint().apply { isAntiAlias = true; isFakeBoldText = true }
        val lightPaint = Paint().apply { isAntiAlias = true; color = Color.GRAY; textSize = 9f }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)

        val document = PdfDocument()
        var pageNumber = 1
        var totalPages = 1

        val firstPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = document.startPage(firstPageInfo)
        var canvas = currentPage.canvas
        var y = 30f

        // Draw watermark if needed
        if (watermark != WatermarkType.NONE) {
            drawWatermark(canvas, watermark, pageWidth.toFloat(), pageHeight.toFloat())
        }

        // === COMPANY HEADER ===
        y = drawCompanyHeader(canvas, paint, boldPaint, lightPaint, context, company, leftMargin, rightMargin, y, isThermal)

        // Separator
        paint.color = Color.parseColor("#0075E8")
        canvas.drawRect(leftMargin, y, rightMargin, y + 2f, paint)
        paint.color = Color.BLACK
        y += 14f

        // === INVOICE TITLE + COPY TYPE ===
        paint.textSize = if (isThermal) 14f else 18f
        boldPaint.textSize = paint.textSize
        canvas.drawText("TAX INVOICE", leftMargin, y, boldPaint)
        if (copyType != PdfCopyType.ORIGINAL) {
            paint.textSize = if (isThermal) 8f else 10f
            paint.color = Color.GRAY
            canvas.drawText(" - ${copyType.displayName}", leftMargin + if (isThermal) 90f else 115f, y, paint)
            paint.color = Color.BLACK
        }
        y += if (isThermal) 20f else 26f

        // === INVOICE DETAILS (Invoice No, Date, Due Date, Payment Status) ===
        paint.textSize = if (isThermal) 9f else 10f
        boldPaint.textSize = paint.textSize
        canvas.drawText("Invoice No: ${invoice.invoiceNumber}", leftMargin, y, boldPaint)
        canvas.drawText("Date: ${dateFormat.format(Date(invoice.invoiceDate))}", rightMargin - 160f, y, paint)
        y += 14f
        invoice.dueDate?.let {
            canvas.drawText("Due Date: ${dateFormat.format(Date(it))}", leftMargin, y, paint)
        }
        canvas.drawText("Payment: ${invoice.paymentStatus.uppercase()}", rightMargin - 160f, y, paint)
        y += 18f

        // === BILL TO / SHIP TO ===
        y = drawBillToShipTo(canvas, paint, boldPaint, lightPaint, invoice, party, leftMargin, rightMargin, y, isThermal)

        // Separator
        paint.color = Color.parseColor("#0075E8")
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        paint.color = Color.BLACK
        y += 12f

        // === ITEMS TABLE ===
        val tableResult = drawItemsTableVyapar(canvas, paint, boldPaint, lightPaint, items, leftMargin, rightMargin, y, isThermal, pageWidth, pageHeight, document, pageNumber, pageNumber, watermark, company, invoice, party, copyType)
        y = tableResult.first
        pageNumber = tableResult.second
        currentPage = tableResult.third
        canvas = currentPage.canvas

        // Re-draw watermark on new page if needed
        if (watermark != WatermarkType.NONE && pageNumber > 1) {
            drawWatermark(canvas, watermark, pageWidth.toFloat(), pageHeight.toFloat())
        }

        y += 12f

        // === TOTALS ===
        y = drawTotalsVyapar(canvas, paint, boldPaint, invoice, leftMargin, rightMargin, y, isThermal)

        // === AMOUNT IN WORDS ===
        y += 6f
        paint.textSize = if (isThermal) 8f else 9f
        boldPaint.textSize = paint.textSize
        canvas.drawText("Amount in Words:", leftMargin, y, boldPaint)
        y += 12f
        paint.textSize = if (isThermal) 8f else 9f
        val amountWords = numberToWords(invoice.totalAmount.toLong())
        canvas.drawText("Rupees $amountWords Only", leftMargin + 8f, y, paint)
        y += 16f

        // === BANK DETAILS + QR CODE (side by side) ===
        y = drawBankDetailsAndQr(canvas, paint, boldPaint, lightPaint, context, company, invoice, leftMargin, rightMargin, y, isThermal)

        // === DECLARATION / TERMS ===
        y = drawDeclarationVyapar(canvas, paint, boldPaint, company, leftMargin, rightMargin, y, isThermal)

        // === MSME NUMBER ===
        company?.msmeUdyamNumber?.let {
            if (!isThermal) {
                paint.textSize = 9f
                boldPaint.textSize = 9f
                canvas.drawText("MSME/Udyam Registration No: $it", leftMargin, y, boldPaint)
                y += 14f
            }
        }

        // === NOTES ===
        invoice.notes?.let {
            y += 4f
            paint.textSize = if (isThermal) 8f else 9f
            canvas.drawText("Notes: $it", leftMargin, y, paint)
            y += 14f
        }

        // === FOOTER ===
        y = drawFooter(canvas, paint, lightPaint, leftMargin, rightMargin, y, isThermal, dateTimeFormat)

        // === CUSTOMER SIGNATURE (A4 only) ===
        if (!isThermal) {
            y += 20f
            canvas.drawLine(rightMargin - 140f, y, rightMargin, y, paint)
            y += 14f
            paint.textSize = 9f
            canvas.drawText("Customer Signature", rightMargin - 130f, y, lightPaint)
        }

        document.finishPage(currentPage)

        val fileName = "Invoice_${invoice.invoiceNumber}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return file
    }

    private fun drawCompanyHeader(
        canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint,
        context: Context, company: CompanyEntity?,
        leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean
    ): Float {
        var y = startY
        var logoHeight = 0f

        company?.logoUri?.let { uriStr ->
            try {
                val uri = android.net.Uri.parse(uriStr)
                val inputStream = context.contentResolver.openInputStream(uri)
                val logoBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (logoBitmap != null) {
                    val logoSize = if (isThermal) 40f else 50f
                    val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize.toInt(), logoSize.toInt(), true)
                    canvas.drawBitmap(scaledLogo, leftMargin, y - logoSize + 4f, paint)
                    scaledLogo.recycle()
                    logoHeight = logoSize
                }
            } catch (_: Exception) {}
        }

        val textOffsetX = if (logoHeight > 0) leftMargin + logoHeight + 10f else leftMargin
        paint.textSize = if (isThermal) 14f else 20f
        boldPaint.textSize = paint.textSize
        canvas.drawText(company?.name ?: "My Business", textOffsetX, y, boldPaint)
        y += if (isThermal) 18f else 28f

        paint.textSize = if (isThermal) 9f else 10f
        company?.address?.let { canvas.drawText(it, textOffsetX, y, paint); y += if (isThermal) 13f else 15f }
        company?.phone?.let { canvas.drawText("Ph: $it", textOffsetX, y, paint); y += if (isThermal) 13f else 15f }
        company?.email?.let { canvas.drawText("Email: $it", textOffsetX, y, paint); y += if (isThermal) 13f else 15f }
        company?.gstin?.let { canvas.drawText("GSTIN: $it", textOffsetX, y, boldPaint); y += if (isThermal) 13f else 15f }
        company?.state?.let { canvas.drawText("State: $it", textOffsetX, y, paint); y += if (isThermal) 13f else 15f }

        y += 6f
        return y
    }

    private fun drawBillToShipTo(
        canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint,
        invoice: InvoiceEntity, party: PartyEntity?,
        leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean
    ): Float {
        var y = startY
        val halfWidth = (rightMargin - leftMargin) / 2f

        if (!isThermal) {
            // Bill To (left side)
            boldPaint.textSize = 10f
            canvas.drawText("Bill To:", leftMargin, y, boldPaint)
            y += 14f
            paint.textSize = 10f
            val partyName = party?.name ?: "Party #${invoice.partyId}"
            canvas.drawText(partyName, leftMargin, y, paint); y += 13f
            party?.phone?.let { canvas.drawText(it, leftMargin, y, paint); y += 13f }
            party?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += 13f }
            party?.address?.let {
                val addr = it.take(60)
                canvas.drawText(addr, leftMargin, y, paint); y += 13f
            }
            val billToY = y

            // Ship To (right side)
            y = startY
            boldPaint.textSize = 10f
            canvas.drawText("Ship To:", leftMargin + halfWidth, y, boldPaint)
            y += 14f
            paint.textSize = 10f
            canvas.drawText(partyName, leftMargin + halfWidth, y, paint); y += 13f
            party?.address?.let {
                val addr = it.take(60)
                canvas.drawText(addr, leftMargin + halfWidth, y, paint); y += 13f
            }

            y = maxOf(billToY, y) + 8f
        } else {
            // Thermal: compact Bill To
            boldPaint.textSize = 9f
            canvas.drawText("Bill To:", leftMargin, y, boldPaint)
            y += 12f
            paint.textSize = 9f
            val partyName = party?.name ?: "Party #${invoice.partyId}"
            canvas.drawText(partyName, leftMargin, y, paint); y += 12f
            party?.phone?.let { canvas.drawText(it, leftMargin, y, paint); y += 12f }
            party?.gstin?.let { canvas.drawText("GSTIN: $it", leftMargin, y, paint); y += 12f }
            y += 4f
        }

        return y
    }

    private fun drawItemsTableVyapar(
        canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint,
        items: List<InvoiceItemEntity>, leftMargin: Float, rightMargin: Float,
        startY: Float, isThermal: Boolean,
        pageWidth: Int, pageHeight: Int, document: PdfDocument,
        currentpageNumber: Int, totalPages: Int,
        watermark: WatermarkType, company: CompanyEntity?, invoice: InvoiceEntity, party: PartyEntity?,
        copyType: PdfCopyType
    ): Triple<Float, Int, PdfDocument.Page> {
        var y = startY
        var pageNumber = currentpageNumber
        var currentPage = document.getPage(pageNumber - 1)
        var canvas = currentPage.canvas

        // Column positions
        val colSNo = leftMargin
        val colItem = leftMargin + (if (isThermal) 20f else 24f)
        val colHsn = leftMargin + (if (isThermal) 160f else 220f)
        val colQty = leftMargin + (if (isThermal) 220f else 310f)
        val colRate = leftMargin + (if (isThermal) 280f else 380f)
        val colAmount = rightMargin - (if (isThermal) 0f else 10f)

        // Header row
        val headerColor = Color.parseColor("#0075E8")
        paint.color = headerColor
        canvas.drawRect(leftMargin, y - 12f, rightMargin, y + 4f, paint)
        paint.color = Color.WHITE
        paint.textSize = if (isThermal) 8f else 9f
        boldPaint.textSize = paint.textSize

        canvas.drawText("#", colSNo, y, boldPaint)
        canvas.drawText("Item", colItem, y, boldPaint)
        if (!isThermal) canvas.drawText("HSN/SAC", colHsn, y, boldPaint)
        canvas.drawText("Qty", colQty, y, boldPaint)
        canvas.drawText("Rate", colRate, y, boldPaint)
        canvas.drawText("Amount", colAmount - 55f, y, boldPaint)

        paint.color = Color.BLACK
        y += 14f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 6f

        // Items
        paint.textSize = if (isThermal) 8f else 9f
        items.forEachIndexed { index, item ->
            val lineHeight = if (isThermal) 12f else 14f

            // Check if we need a new page (A4 only)
            if (!isThermal && y + lineHeight * 2 > pageHeight - 100f) {
                document.finishPage(currentPage)
                pageNumber++
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas

                if (watermark != WatermarkType.NONE) {
                    drawWatermark(canvas, watermark, pageWidth.toFloat(), pageHeight.toFloat())
                }

                // Repeat table header on new page
                y = 30f
                paint.color = headerColor
                canvas.drawRect(leftMargin, y - 12f, rightMargin, y + 4f, paint)
                paint.color = Color.WHITE
                paint.textSize = 9f; boldPaint.textSize = 9f
                canvas.drawText("#", colSNo, y, boldPaint)
                canvas.drawText("Item", colItem, y, boldPaint)
                canvas.drawText("HSN/SAC", colHsn, y, boldPaint)
                canvas.drawText("Qty", colQty, y, boldPaint)
                canvas.drawText("Rate", colRate, y, boldPaint)
                canvas.drawText("Amount", colAmount - 55f, y, boldPaint)
                paint.color = Color.BLACK
                y += 14f
                canvas.drawLine(leftMargin, y, rightMargin, y, paint)
                y += 6f
                paint.textSize = 9f
            }

            // S.No
            canvas.drawText("${index + 1}", colSNo, y, paint)
            // Item name (with description below if present)
            val maxNameLen = if (isThermal) 18 else 25
            canvas.drawText(item.itemName.take(maxNameLen), colItem, y, paint)
            // HSN Code
            if (!isThermal) {
                item.hsnCode?.let { canvas.drawText(it, colHsn, y, paint) }
            }
            // Qty + Unit
            canvas.drawText("${item.quantity.toInt()} ${item.unit}", colQty, y, paint)
            // Rate
            canvas.drawText(String.format(Locale.US, "%.2f", item.price), colRate, y, paint)
            // Amount
            canvas.drawText(String.format(Locale.US, "%.2f", item.totalAmount), colAmount - 55f, y, paint)
            y += lineHeight

            // Description (if present, A4 only)
            if (!isThermal && !item.description.isNullOrBlank()) {
                paint.textSize = 8f
                lightPaint.textSize = 8f
                canvas.drawText(item.description.take(50), colItem, y, lightPaint)
                paint.textSize = 9f
                y += 10f
            }

            // GST breakdown per item (CGST + SGST)
            if (!isThermal) {
                paint.textSize = 7f
                lightPaint.textSize = 7f
                val halfGst = item.gstRate / 2.0
                val cgstAmount = item.totalAmount * halfGst / 100.0
                val sgstAmount = item.totalAmount * halfGst / 100.0
                val gstText = "GST ${item.gstRate.toInt()}% (CGST: ${String.format(Locale.US, "%.1f", halfGst)}% = ${String.format(Locale.US, "%.2f", cgstAmount)} + SGST: ${String.format(Locale.US, "%.1f", halfGst)}% = ${String.format(Locale.US, "%.2f", sgstAmount)})"
                canvas.drawText(gstText, colItem, y, lightPaint)
                paint.textSize = 9f
                y += 9f
            }

            // Subtle row separator
            lightPaint.color = Color.parseColor("#F0F0F0")
            canvas.drawLine(leftMargin, y, rightMargin, y, lightPaint)
            lightPaint.color = Color.GRAY
            y += 3f
        }

        y += 5f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 4f

        return Triple(y, pageNumber, currentPage)
    }

    private fun drawTotalsVyapar(
        canvas: Canvas, paint: Paint, boldPaint: Paint, invoice: InvoiceEntity,
        leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean
    ): Float {
        var y = startY
        val labelX = rightMargin - 180f
        val valueX = rightMargin - 10f

        fun drawLine(label: String, value: String, isBold: Boolean = false, isNegative: Boolean = false) {
            val p = if (isBold) boldPaint else paint
            p.textSize = if (isBold) (if (isThermal) 10f else 12f) else (if (isThermal) 9f else 11f)
            canvas.drawText(label, labelX, y, p)
            val displayValue = if (isNegative) "-$value" else value
            canvas.drawText(displayValue, valueX - 60f, y, p)
            y += if (isThermal) 13f else 16f
        }

        // Subtotal
        drawLine("Subtotal:", String.format(Locale.US, "%.2f", invoice.subTotal))

        // Discount
        if (invoice.discount > 0) {
            drawLine("Discount:", String.format(Locale.US, "%.2f", invoice.discount), isNegative = true)
        }

        // Tax breakdown
        if (invoice.igstTotal > 0) {
            drawLine("IGST:", String.format(Locale.US, "%.2f", invoice.igstTotal))
        } else {
            if (invoice.cgstTotal > 0) drawLine("CGST:", String.format(Locale.US, "%.2f", invoice.cgstTotal))
            if (invoice.sgstTotal > 0) drawLine("SGST:", String.format(Locale.US, "%.2f", invoice.sgstTotal))
        }

        // Additional charges
        if (invoice.shippingCharges > 0) drawLine("Shipping:", String.format(Locale.US, "%.2f", invoice.shippingCharges))
        if (invoice.packagingCharges > 0) drawLine("Packaging:", String.format(Locale.US, "%.2f", invoice.packagingCharges))
        if (invoice.insuranceCharges > 0) drawLine("Insurance:", String.format(Locale.US, "%.2f", invoice.insuranceCharges))

        // TCS/TDS
        if (invoice.tcsAmount > 0) drawLine("TCS (${invoice.tcsRate}%):", String.format(Locale.US, "%.2f", invoice.tcsAmount))
        if (invoice.tdsAmount > 0) drawLine("TDS (${invoice.tdsRate}%):", String.format(Locale.US, "%.2f", invoice.tdsAmount))

        // Round Off
        if (invoice.roundOff != 0.0) drawLine("Round Off:", String.format(Locale.US, "%.2f", invoice.roundOff))

        // Total line
        y += 3f
        val totalColor = Color.parseColor("#0075E8")
        paint.color = totalColor
        canvas.drawLine(labelX, y, rightMargin, y, paint)
        paint.color = Color.BLACK
        y += 18f

        // Total amount
        paint.textSize = if (isThermal) 12f else 16f
        boldPaint.textSize = paint.textSize
        canvas.drawText("TOTAL:", labelX, y, boldPaint)
        canvas.drawText(String.format(Locale.US, "\u20B9%.2f", invoice.totalAmount), valueX - 80f, y, boldPaint)
        y += 18f

        // Amount received / balance
        if (invoice.amountPaid > 0) {
            paint.textSize = if (isThermal) 9f else 11f
            canvas.drawText("Amount Received:", labelX, y, paint)
            canvas.drawText(String.format(Locale.US, "\u20B9%.2f", invoice.amountPaid), valueX - 80f, paint)
            y += 14f
            val balance = invoice.totalAmount - invoice.amountPaid
            if (balance > 0) {
                boldPaint.textSize = if (isThermal) 10f else 12f
                canvas.drawText("Balance Due:", labelX, y, boldPaint)
                canvas.drawText(String.format(Locale.US, "\u20B9%.2f", balance), valueX - 80f, boldPaint)
                y += 16f
            }
        }

        return y
    }

    private fun drawBankDetailsAndQr(
        canvas: Canvas, paint: Paint, boldPaint: Paint, lightPaint: Paint,
        context: Context, company: CompanyEntity?, invoice: InvoiceEntity,
        leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean
    ): Float {
        var y = startY
        val hasBankDetails = !company?.bankName.isNullOrBlank() || !company?.bankAccountNumber.isNullOrBlank()

        if (hasBankDetails && !isThermal) {
            // Bank Details on left
            paint.textSize = 11f; boldPaint.textSize = 11f
            canvas.drawText("Bank Details", leftMargin, y, boldPaint)
            y += 14f

            paint.textSize = 9f
            company?.bankName?.let { canvas.drawText("Bank: $it", leftMargin, y, paint); y += 13f }
            company?.bankBranch?.let { canvas.drawText("Branch: $it", leftMargin, y, paint); y += 13f }
            company?.bankAccountNumber?.let { canvas.drawText("A/C No: $it", leftMargin, y, paint); y += 13f }
            company?.bankIfsc?.let { canvas.drawText("IFSC: $it", leftMargin, y, paint); y += 13f }
            company?.bankUpiId?.let { canvas.drawText("UPI: $it", leftMargin, y, paint); y += 13f }

            // QR Code on right side
            val upiId = company?.bankUpiId
            if (!upiId.isNullOrBlank()) {
                try {
                    val upiString = "upi://pay?pa=$upiId&pn=${company?.name ?: "Business"}&am=${String.format(Locale.US, "%.2f", invoice.totalAmount)}&cu=INR&tn=Payment for ${invoice.invoiceNumber}"
                    val qrBitmap = generateQrCode(upiString, 120)
                    val qrLeft = rightMargin - 130f
                    val qrY = startY - 10f
                    canvas.drawBitmap(qrBitmap, null, RectF(qrLeft, qrY, qrLeft + 110f, qrY + 110f), paint)
                    paint.textSize = 8f
                    canvas.drawText("Scan to Pay", qrLeft + 25f, qrY + 122f, lightPaint)
                    paint.textSize = 9f

                    // Payment app logos
                    val paymentLogos = listOf(
                        R.drawable.ic_paytm to "Paytm",
                        R.drawable.ic_phonepe to "PhonePe",
                        R.drawable.ic_gpay to "GPay",
                        R.drawable.ic_bhim to "BHIM"
                    )
                    val logoSize = 14f
                    val spacing = 4f
                    val totalWidth = paymentLogos.size * logoSize + (paymentLogos.size - 1) * spacing
                    var logoX = qrLeft + (110f - totalWidth) / 2f
                    val logoY = qrY + 128f

                    paymentLogos.forEach { (resId, _) ->
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
                } catch (_: Exception) {}
            }

            y += 10f
        } else if (hasBankDetails && isThermal) {
            // Thermal: compact bank details
            paint.textSize = 9f; boldPaint.textSize = 9f
            canvas.drawText("Bank Details:", leftMargin, y, boldPaint)
            y += 12f
            paint.textSize = 8f
            company?.bankName?.let { canvas.drawText("Bank: $it", leftMargin, y, paint); y += 11f }
            company?.bankAccountNumber?.let { canvas.drawText("A/C: $it", leftMargin, y, paint); y += 11f }
            company?.bankIfsc?.let { canvas.drawText("IFSC: $it", leftMargin, y, paint); y += 11f }
            company?.bankUpiId?.let { canvas.drawText("UPI: $it", leftMargin, y, paint); y += 11f }
            y += 4f
        }

        return y
    }

    private fun drawDeclarationVyapar(
        canvas: Canvas, paint: Paint, boldPaint: Paint, company: CompanyEntity?,
        leftMargin: Float, rightMargin: Float, startY: Float, isThermal: Boolean
    ): Float {
        val declarationText = company?.declaration
        if (declarationText.isNullOrBlank()) return startY

        var y = startY
        paint.textSize = if (isThermal) 9f else 11f; boldPaint.textSize = paint.textSize
        canvas.drawText("Terms and Conditions:", leftMargin, y, boldPaint)
        y += 16f

        paint.textSize = if (isThermal) 7f else 9f
        val lines = declarationText.split("\n").filter { it.isNotBlank() }
        lines.forEachIndexed { index, line ->
            val numberedLine = "${index + 1}. ${line.trim()}"
            val maxChars = if (isThermal) 50 else 80
            canvas.drawText(numberedLine.take(maxChars), leftMargin + 8f, y, paint)
            y += 13f
        }
        y += 6f
        return y
    }

    private fun drawFooter(
        canvas: Canvas, paint: Paint, lightPaint: Paint,
        leftMargin: Float, rightMargin: Float, startY: Float,
        isThermal: Boolean, dateFormat: SimpleDateFormat
    ): Float {
        var y = startY + 4f
        canvas.drawLine(leftMargin, y, rightMargin, y, paint)
        y += 12f
        paint.textSize = if (isThermal) 7f else 8f
        lightPaint.textSize = paint.textSize
        canvas.drawText("Powered by Mimo GST Billing  |  ${dateFormat.format(Date())}", leftMargin, y, lightPaint)
        return y + 10f
    }

    private fun drawWatermark(canvas: Canvas, watermark: WatermarkType, pageWidth: Float, pageHeight: Float) {
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 80f
            color = when (watermark) {
                WatermarkType.PAID -> Color.parseColor("#2008BD7C")
                WatermarkType.UNPAID -> Color.parseColor("#20ED1A3B")
                WatermarkType.DRAFT -> Color.parseColor("#2071748E")
                WatermarkType.CANCELLED -> Color.parseColor("#20ED1A3B")
                WatermarkType.OVERDUE -> Color.parseColor("#20FF8A00")
                WatermarkType.PARTIAL -> Color.parseColor("#20FF8A00")
                else -> Color.TRANSPARENT
            }
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        canvas.save()
        canvas.rotate(-45f, pageWidth / 2f, pageHeight / 2f)
        canvas.drawText(watermark.displayName.uppercase(), pageWidth / 2f, pageHeight / 2f, paint)
        canvas.restore()
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
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun numberToWords(number: Long): String {
        if (number == 0L) return "Zero"
        val ones = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        fun convert(n: Long): String {
            return when {
                n < 20 -> ones[n.toInt()]
                n < 100 -> tens[(n / 10).toInt()] + if (n % 10 != 0L) " ${ones[(n % 10).toInt()]}" else ""
                n < 1000 -> ones[(n / 100).toInt()] + " Hundred" + if (n % 100 != 0L) " and ${convert(n % 100)}" else ""
                n < 100000 -> convert(n / 1000) + " Thousand" + if (n % 1000 != 0L) " ${convert(n % 1000)}" else ""
                n < 10000000 -> convert(n / 100000) + " Lakh" + if (n % 100000 != 0L) " ${convert(n % 100000)}" else ""
                else -> convert(n / 10000000) + " Crore" + if (n % 10000000 != 0L) " ${convert(n % 10000000)}" else ""
            }
        }
        return convert(abs(number))
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

    fun sharePdfToWhatsApp(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            sharePdf(context, file)
        }
    }

    fun printPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun savePdf(context: Context, file: File): Boolean {
        return try {
            val downloadsDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "MimoGST")
            downloadsDir.mkdirs()
            val destFile = File(downloadsDir, file.name)
            file.copyTo(destFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }
}

package com.mimo.gstbilling.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.EWayBillDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.EWayBillEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EWayBillViewModel @Inject constructor(
    private val ewayBillDao: EWayBillDao,
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _companyId = MutableStateFlow(1L)

    val ewayBills: StateFlow<List<EWayBillEntity>> = _companyId.flatMapLatest { companyId ->
        ewayBillDao.getEWayBills(companyId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeEwayBills: StateFlow<List<EWayBillEntity>> = _companyId.flatMapLatest { companyId ->
        ewayBillDao.getActiveEWayBills(companyId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = _companyId.flatMapLatest { companyId ->
        ewayBillDao.getActiveCount(companyId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _generatedEwb = MutableStateFlow<EWayBillEntity?>(null)
    val generatedEwb: StateFlow<EWayBillEntity?> = _generatedEwb

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().collect { company ->
                _companyId.value = company?.id ?: 1L
            }
        }
    }

    fun generateEWayBill(
        invoiceNumber: String,
        partyName: String,
        partyGstin: String?,
        placeOfSupply: String,
        invoiceValue: Double,
        hsnCode: String,
        transporterName: String?,
        transporterGstin: String?,
        vehicleNumber: String?,
        distance: Int?,
        supplyType: String,
        subSupplyType: String
    ) {
        viewModelScope.launch {
            _isGenerating.value = true

            // Generate proper EWB number: 12-digit based on state code + date + serial
            val stateCode = getStateCode(placeOfSupply)
            val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val serialPart = String.format("%04d", (System.currentTimeMillis() % 10000))
            val ewbNumber = "$stateCode$datePart$serialPart"

            // Generate QR code data
            val qrData = "EWB:$ewbNumber|INV:$invoiceNumber|FROM:${getCompanyGstin()}|TO:${partyGstin ?: "URD"}|VALUE:$invoiceValue|DATE:${SimpleDateFormat("ddMMyyyy", Locale.US).format(Date())}"

            val qrBitmap = generateQrCode(qrData)

            val ewayBill = EWayBillEntity(
                companyId = _companyId.value,
                ewbNumber = ewbNumber,
                invoiceNumber = invoiceNumber,
                partyName = partyName,
                partyGstin = partyGstin,
                placeOfSupply = placeOfSupply,
                invoiceValue = invoiceValue,
                hsnCode = hsnCode,
                transporterName = transporterName,
                transporterGstin = transporterGstin,
                vehicleNumber = vehicleNumber,
                distance = distance,
                supplyType = supplyType,
                subSupplyType = subSupplyType,
                generatedDate = System.currentTimeMillis(),
                validUntil = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours validity
                qrCodeData = qrData
            )

            val id = ewayBillDao.insert(ewayBill)
            _generatedEwb.value = ewayBill.copy(id = id)
            _isGenerating.value = false
        }
    }

    fun cancelEWayBill(id: Long) {
        viewModelScope.launch {
            ewayBillDao.cancelEWayBill(id)
        }
    }

    fun generateEwbPdf(context: Context, ewb: EWayBillEntity): java.io.File? {
        return try {
            val pageWidth = 595
            val pageHeight = 842
            val document = android.graphics.pdf.PdfDocument()

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = android.graphics.Paint().apply { isAntiAlias = true }

            // Header
            paint.textSize = 20f
            paint.color = android.graphics.Color.parseColor("#1565C0")
            paint.isFakeBoldText = true
            canvas.drawText("E-WAY BILL", 40f, 50f, paint)

            paint.textSize = 12f
            paint.color = android.graphics.Color.DKGRAY
            paint.isFakeBoldText = false
            canvas.drawText("Generated by Mimo GST Billing", 40f, 70f, paint)

            // Divider
            paint.color = android.graphics.Color.parseColor("#1565C0")
            canvas.drawLine(40f, 85f, (pageWidth - 40).toFloat(), 85f, paint)

            // EWB Details
            var y = 120f
            paint.textSize = 14f
            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = true
            canvas.drawText("E-WAY BILL DETAILS", 40f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 12f

            fun drawRow(label: String, value: String) {
                paint.color = android.graphics.Color.GRAY
                canvas.drawText(label, 40f, y, paint)
                paint.color = android.graphics.Color.BLACK
                canvas.drawText(value, 200f, y, paint)
                y += 22f
            }

            drawRow("EWB Number:", ewb.ewbNumber)
            drawRow("Invoice Number:", ewb.invoiceNumber)
            drawRow("Party:", ewb.partyName)
            drawRow("Party GSTIN:", ewb.partyGstin ?: "N/A")
            drawRow("Place of Supply:", ewb.placeOfSupply)
            drawRow("Invoice Value:", String.format(Locale.US, "\u20B9%,.2f", ewb.invoiceValue))
            drawRow("HSN Code:", ewb.hsnCode)
            drawRow("Supply Type:", ewb.supplyType)
            drawRow("Sub Supply Type:", ewb.subSupplyType)

            y += 10f
            canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, android.graphics.Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 1f })
            y += 20f

            paint.isFakeBoldText = true
            paint.textSize = 14f
            paint.color = android.graphics.Color.BLACK
            canvas.drawText("TRANSPORTER DETAILS", 40f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 12f

            drawRow("Transporter:", ewb.transporterName ?: "N/A")
            drawRow("Transporter GSTIN:", ewb.transporterGstin ?: "N/A")
            drawRow("Vehicle Number:", ewb.vehicleNumber ?: "N/A")
            drawRow("Distance:", if (ewb.distance != null) "${ewb.distance} km" else "N/A")

            y += 10f
            canvas.drawLine(40f, y, (pageWidth - 40).toFloat(), y, android.graphics.Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 1f })
            y += 20f

            val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US)
            paint.isFakeBoldText = true
            paint.textSize = 14f
            canvas.drawText("VALIDITY", 40f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 12f

            drawRow("Generated:", dateFormat.format(java.util.Date(ewb.generatedDate)))
            drawRow("Valid Until:", dateFormat.format(java.util.Date(ewb.validUntil)))
            drawRow("Status:", ewb.status)

            // QR Code
            if (ewb.qrCodeData != null) {
                y += 20f
                try {
                    val qrFile = java.io.File(ewb.qrCodeData)
                    if (qrFile.exists()) {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(ewb.qrCodeData)
                        bitmap?.let {
                            val scaled = android.graphics.Bitmap.createScaledBitmap(it, 120, 120, true)
                            canvas.drawBitmap(scaled, 40f, y, null)
                            paint.textSize = 10f
                            paint.color = android.graphics.Color.GRAY
                            canvas.drawText("Scan for E-Way Bill details", 170f, y + 60f, paint)
                        }
                    }
                } catch (_: Exception) {}
            }

            // Footer
            paint.textSize = 9f
            paint.color = android.graphics.Color.GRAY
            canvas.drawText("This is a computer-generated document. No signature required.", 40f, (pageHeight - 30).toFloat(), paint)

            document.finishPage(page)

            val file = java.io.File(context.cacheDir, "ewb_${ewb.ewbNumber}.pdf")
            file.outputStream().use { document.writeTo(it) }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getStateCode(stateName: String): String {
        val states = mapOf(
            "Andhra Pradesh" to "37", "Arunachal Pradesh" to "12", "Assam" to "18", "Bihar" to "10",
            "Chhattisgarh" to "22", "Goa" to "30", "Gujarat" to "24", "Haryana" to "06",
            "Himachal Pradesh" to "02", "Jharkhand" to "20", "Karnataka" to "29", "Kerala" to "32",
            "Madhya Pradesh" to "23", "Maharashtra" to "27", "Manipur" to "14", "Meghalaya" to "17",
            "Mizoram" to "15", "Nagaland" to "13", "Odisha" to "21", "Punjab" to "03",
            "Rajasthan" to "08", "Sikkim" to "11", "Tamil Nadu" to "33", "Telangana" to "36",
            "Tripura" to "16", "Uttar Pradesh" to "09", "Uttarakhand" to "05", "West Bengal" to "19",
            "Delhi" to "07", "Chandigarh" to "04"
        )
        return states[stateName] ?: "99"
    }

    private suspend fun getCompanyGstin(): String {
        // In a real app, fetch from company settings
        return "27AABCU9603R1ZM"
    }

    private fun generateQrCode(data: String): String {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 400, 400)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            // Save to cache and return path
            val file = java.io.File(context.cacheDir, "ewb_qr_${System.currentTimeMillis()}.png")
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }
}

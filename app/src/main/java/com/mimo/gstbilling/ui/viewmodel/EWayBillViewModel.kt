package com.mimo.gstbilling.ui.viewmodel

import android.content.Context
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

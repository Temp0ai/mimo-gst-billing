package com.mimo.gstbilling.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CaAccessDao
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.InvoiceItemDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.entity.CaAccessEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CaShareViewModel @Inject constructor(
    private val caAccessDao: CaAccessDao,
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val partyDao: PartyDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _companyId = MutableStateFlow(1L)

    val caList: StateFlow<List<CaAccessEntity>> = _companyId.flatMapLatest { companyId ->
        caAccessDao.getAllCaList(companyId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCaList: StateFlow<List<CaAccessEntity>> = _companyId.flatMapLatest { companyId ->
        caAccessDao.getActiveCaList(companyId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _shareResult = MutableSharedFlow<String>()
    val shareResult: SharedFlow<String> = _shareResult

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().collect { company ->
                _companyId.value = company?.id ?: 1L
            }
        }
    }

    fun addCa(caName: String, caEmail: String, caPhone: String?, caGstin: String?, firmName: String?, accessLevel: String) {
        viewModelScope.launch {
            caAccessDao.insert(
                CaAccessEntity(
                    companyId = _companyId.value,
                    caName = caName.trim(),
                    caEmail = caEmail.trim(),
                    caPhone = caPhone?.trim()?.ifBlank { null },
                    caGstin = caGstin?.trim()?.ifBlank { null },
                    firmName = firmName?.trim()?.ifBlank { null },
                    accessLevel = accessLevel
                )
            )
        }
    }

    fun updateCa(ca: CaAccessEntity) {
        viewModelScope.launch { caAccessDao.update(ca) }
    }

    fun deleteCa(ca: CaAccessEntity) {
        viewModelScope.launch { caAccessDao.delete(ca) }
    }

    fun shareGstr1WithCa(ca: CaAccessEntity) {
        viewModelScope.launch {
            try {
                val invoices = invoiceDao.getInvoicesByCompany(_companyId.value).first()
                if (invoices.isEmpty()) {
                    _shareResult.emit("No invoices to share")
                    return@launch
                }
                val summary = buildString {
                    appendLine("GSTR-1 Summary for ${SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())}")
                    appendLine("Total Invoices: ${invoices.size}")
                    appendLine("Total Sales: \u20B9${String.format(Locale.US, "%,.2f", invoices.sumOf { it.totalAmount })}")
                    appendLine("Total Tax: \u20B9${String.format(Locale.US, "%,.2f", invoices.sumOf { it.cgstTotal + it.sgstTotal + it.igstTotal })}")
                    invoices.forEach { inv ->
                        appendLine("${inv.invoiceNumber} | ${SimpleDateFormat("dd/MM", Locale.US).format(Date(inv.invoiceDate))} | \u20B9${String.format(Locale.US, "%,.2f", inv.totalAmount)}")
                    }
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, summary)
                    putExtra(Intent.EXTRA_SUBJECT, "GSTR-1 Report")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share with ${ca.caName}"))
                caAccessDao.updateLastShared(ca.id, System.currentTimeMillis())
                _shareResult.emit("GSTR-1 shared with ${ca.caName}")
            } catch (e: Exception) {
                _shareResult.emit("Error: ${e.message}")
            }
        }
    }

    fun shareAllDataWithCa(ca: CaAccessEntity) {
        viewModelScope.launch {
            try {
                val invoices = invoiceDao.getInvoicesByCompany(_companyId.value).first()
                val parties = partyDao.getPartiesByCompany(_companyId.value).first()
                val summary = buildString {
                    appendLine("Business Data Export")
                    appendLine("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())}")
                    appendLine("---")
                    appendLine("Parties: ${parties.size}")
                    appendLine("Invoices: ${invoices.size}")
                    appendLine("Total Sales: \u20B9${String.format(Locale.US, "%,.2f", invoices.sumOf { it.totalAmount })}")
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, summary)
                    putExtra(Intent.EXTRA_SUBJECT, "Business Data Export")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share with ${ca.caName}"))
                caAccessDao.updateLastShared(ca.id, System.currentTimeMillis())
                _shareResult.emit("Data shared with ${ca.caName}")
            } catch (e: Exception) {
                _shareResult.emit("Error: ${e.message}")
            }
        }
    }
}

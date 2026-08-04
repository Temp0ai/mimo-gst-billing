package com.mimo.gstbilling.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CaAccessDao
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.InvoiceItemDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.entity.CaAccessEntity
import com.mimo.gstbilling.utils.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
                PdfGenerator.generateGstr1Pdf(context, invoices, invoiceItemDao, partyDao)
                caAccessDao.updateLastShared(ca.id, System.currentTimeMillis())
                _shareResult.emit("GSTR-1 report generated. Choose share method.")
            } catch (e: Exception) {
                _shareResult.emit("Error: ${e.message}")
            }
        }
    }

    fun shareAllDataWithCa(ca: CaAccessEntity) {
        viewModelScope.launch {
            try {
                val invoices = invoiceDao.getInvoicesByCompany(_companyId.value).first()
                PdfGenerator.generateGstr1Pdf(context, invoices, invoiceItemDao, partyDao)
                caAccessDao.updateLastShared(ca.id, System.currentTimeMillis())
                _shareResult.emit("All data exported. Choose share method.")
            } catch (e: Exception) {
                _shareResult.emit("Error: ${e.message}")
            }
        }
    }
}

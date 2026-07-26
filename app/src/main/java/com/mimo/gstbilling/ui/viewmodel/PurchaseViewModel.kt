package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    private val _purchaseInvoices = MutableStateFlow<List<InvoiceEntity>>(emptyList())
    val purchaseInvoices: StateFlow<List<InvoiceEntity>> = _purchaseInvoices.asStateFlow()

    private val _totalPurchases = MutableStateFlow(0.0)
    val totalPurchases: StateFlow<Double> = _totalPurchases.asStateFlow()

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount.asStateFlow()

    private val _pendingAmount = MutableStateFlow(0.0)
    val pendingAmount: StateFlow<Double> = _pendingAmount.asStateFlow()

    init {
        loadPurchaseInvoices()
        loadSummary()
    }

    private fun loadPurchaseInvoices() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            invoiceDao.getInvoicesByType(cId, "purchase").collect { _purchaseInvoices.value = it }
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            _totalPurchases.value = invoiceDao.getTotalPurchases(cId) ?: 0.0
            _paidAmount.value = invoiceDao.getPaidTotal(cId) ?: 0.0
            _pendingAmount.value = invoiceDao.getPendingPayables(cId) ?: 0.0
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            invoiceDao.deleteInvoice(invoice)
            loadPurchaseInvoices()
            loadSummary()
        }
    }
}

package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    private val companyId = 1L

    val purchaseInvoices: StateFlow<List<InvoiceEntity>> = invoiceDao.getInvoicesByType(companyId, "purchase")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalPurchases = MutableStateFlow(0.0)
    val totalPurchases: StateFlow<Double> = _totalPurchases.asStateFlow()

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount.asStateFlow()

    private val _pendingAmount = MutableStateFlow(0.0)
    val pendingAmount: StateFlow<Double> = _pendingAmount.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            _totalPurchases.value = invoiceDao.getTotalPurchases(companyId) ?: 0.0
            _paidAmount.value = invoiceDao.getPaidTotal(companyId) ?: 0.0
            _pendingAmount.value = invoiceDao.getPendingPayables(companyId) ?: 0.0
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            invoiceDao.deleteInvoice(invoice)
            loadSummary()
        }
    }
}

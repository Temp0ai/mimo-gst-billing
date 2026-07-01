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
class SalesViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    private val companyId = 1L

    val salesInvoices: StateFlow<List<InvoiceEntity>> = invoiceDao.getInvoicesByType(companyId, "sales")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalSales = MutableStateFlow(0.0)
    val totalSales: StateFlow<Double> = _totalSales.asStateFlow()

    private val _collectedAmount = MutableStateFlow(0.0)
    val collectedAmount: StateFlow<Double> = _collectedAmount.asStateFlow()

    private val _pendingAmount = MutableStateFlow(0.0)
    val pendingAmount: StateFlow<Double> = _pendingAmount.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            _totalSales.value = invoiceDao.getTotalSales(companyId) ?: 0.0
            _collectedAmount.value = invoiceDao.getCollectedTotal(companyId) ?: 0.0
            _pendingAmount.value = invoiceDao.getPendingReceivables(companyId) ?: 0.0
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            invoiceDao.deleteInvoice(invoice)
            loadSummary()
        }
    }
}

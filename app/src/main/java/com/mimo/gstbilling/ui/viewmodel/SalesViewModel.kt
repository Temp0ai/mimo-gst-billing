package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.utils.RecycleBinHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val companyDao: CompanyDao,
    private val recycleBinHelper: RecycleBinHelper
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    private val _salesInvoices = MutableStateFlow<List<InvoiceEntity>>(emptyList())
    val salesInvoices: StateFlow<List<InvoiceEntity>> = _salesInvoices.asStateFlow()

    private val _totalSales = MutableStateFlow(0.0)
    val totalSales: StateFlow<Double> = _totalSales.asStateFlow()

    private val _collectedAmount = MutableStateFlow(0.0)
    val collectedAmount: StateFlow<Double> = _collectedAmount.asStateFlow()

    private val _pendingAmount = MutableStateFlow(0.0)
    val pendingAmount: StateFlow<Double> = _pendingAmount.asStateFlow()

    init {
        loadSalesInvoices()
        loadSummary()
    }

    private fun loadSalesInvoices() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            invoiceDao.getInvoicesByType(cId, "sales").collect { _salesInvoices.value = it }
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            _totalSales.value = invoiceDao.getTotalSales(cId) ?: 0.0
            _collectedAmount.value = invoiceDao.getCollectedTotal(cId) ?: 0.0
            _pendingAmount.value = invoiceDao.getPendingReceivables(cId) ?: 0.0
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            recycleBinHelper.deleteInvoiceToBin(invoice)
            loadSalesInvoices()
            loadSummary()
        }
    }
}

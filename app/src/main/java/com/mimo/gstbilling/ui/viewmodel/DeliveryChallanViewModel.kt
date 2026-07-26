package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.InvoiceItemDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryChallanViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val challans: StateFlow<List<InvoiceEntity>> = flow { emit(getCurrentCompanyId()) }
        .flatMapLatest { id -> invoiceDao.getInvoicesByCompany(id) }
        .map { invoices -> invoices.filter { it.invoiceType == "delivery_challan" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteChallan(challanId: Long) {
        viewModelScope.launch {
            invoiceDao.getInvoiceById(challanId)?.let { invoiceDao.deleteInvoice(it) }
        }
    }

    suspend fun getItemsForChallan(challanId: Long): List<InvoiceItemEntity> {
        return invoiceItemDao.getItemsForInvoice(challanId)
    }
}

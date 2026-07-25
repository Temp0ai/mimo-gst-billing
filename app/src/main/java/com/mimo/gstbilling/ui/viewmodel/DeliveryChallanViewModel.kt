package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryChallanViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    private val companyId = 1L

    val challans: StateFlow<List<InvoiceEntity>> = invoiceDao.getInvoicesByCompany(companyId)
        .map { invoices -> invoices.filter { it.invoiceType == "delivery_challan" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteChallan(challanId: Long) {
        viewModelScope.launch {
            invoiceDao.getInvoiceById(challanId)?.let { invoiceDao.deleteInvoice(it) }
        }
    }

    suspend fun getItemsForChallan(challanId: Long): List<InvoiceItemEntity> {
        return invoiceDao.getItemsForInvoice(challanId)
    }
}

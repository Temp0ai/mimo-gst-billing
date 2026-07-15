package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CreditNoteViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    private val companyId = 1L

    val creditNotes: StateFlow<List<InvoiceEntity>> = invoiceDao.getInvoicesByCompany(companyId)
        .map { invoices -> invoices.filter { it.invoiceType == "credit_note" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

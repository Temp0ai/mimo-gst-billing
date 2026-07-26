package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DebitNoteViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val debitNotes: StateFlow<List<InvoiceEntity>> = companyDao.getSelectedCompany()
        .flatMapLatest { company ->
            invoiceDao.getInvoicesByCompany(company?.id ?: 1L)
        }
        .map { invoices -> invoices.filter { it.invoiceType == "debit_note" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

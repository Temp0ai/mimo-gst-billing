package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.RecurringInvoiceDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.entity.RecurringInvoiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RecurringInvoiceViewModel @Inject constructor(
    private val recurringDao: RecurringInvoiceDao,
    private val partyDao: PartyDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val recurring: StateFlow<List<RecurringInvoiceEntity>> = flow { emit(getCurrentCompanyId()) }
        .flatMapLatest { id -> recurringDao.getRecurringByCompany(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parties = flow { emit(getCurrentCompanyId()) }
        .flatMapLatest { id -> partyDao.getPartiesByCompany(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecurring(partyId: Long, partyName: String, frequency: String, amount: Double, description: String?, invoiceType: String, nextDueDate: Long) {
        viewModelScope.launch {
            recurringDao.insertRecurring(RecurringInvoiceEntity(companyId = getCurrentCompanyId(), partyId = partyId, partyName = partyName, frequency = frequency, amount = amount, description = description, invoiceType = invoiceType, nextDueDate = nextDueDate))
        }
    }

    fun toggleActive(recurring: RecurringInvoiceEntity) {
        viewModelScope.launch { recurringDao.updateRecurring(recurring.copy(isActive = !recurring.isActive)) }
    }

    fun deleteRecurring(recurring: RecurringInvoiceEntity) {
        viewModelScope.launch { recurringDao.deleteRecurring(recurring) }
    }
}

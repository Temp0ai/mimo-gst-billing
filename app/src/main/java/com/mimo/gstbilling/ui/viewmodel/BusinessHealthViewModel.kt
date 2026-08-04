package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.BusinessHealthScorer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BusinessHealthUiState(
    val health: BusinessHealthScorer.HealthScore = BusinessHealthScorer.HealthScore(0,0,0,0,0,"No Data",emptyList(),emptyList()),
    val isLoading: Boolean = true
)

@HiltViewModel
class BusinessHealthViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao,
    private val expenseDao: ExpenseDao,
    private val partyDao: PartyDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(BusinessHealthUiState())
    val uiState: StateFlow<BusinessHealthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val expenses = expenseDao.getExpensesByCompany(companyId).first()
            val parties = partyDao.getPartiesByCompany(companyId).first()
            _uiState.value = BusinessHealthUiState(
                health = BusinessHealthScorer.score(invoices, expenses, parties),
                isLoading = false
            )
        }
    }
}
package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ExpenseDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.utils.AiTaxSavingAdvisor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiTaxAdvisorUiState(
    val gstSummary: Map<String, Double> = emptyMap(),
    val suggestions: List<AiTaxSavingAdvisor.TaxSuggestion> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AiTaxAdvisorViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiTaxAdvisorUiState())
    val uiState: StateFlow<AiTaxAdvisorUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    init {
        analyzeTaxSavings()
    }

    fun analyzeTaxSavings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val expenses = expenseDao.getExpensesByCompany(companyId).first()

            val gstSummary = AiTaxSavingAdvisor.calculateGstSummary(invoices)
            val taxSavings = AiTaxSavingAdvisor.analyzeTaxSavings(invoices, expenses, emptyMap())

            _uiState.value = AiTaxAdvisorUiState(
                gstSummary = gstSummary,
                suggestions = taxSavings.suggestions,
                isLoading = false
            )
        }
    }
}

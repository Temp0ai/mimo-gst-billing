package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ExpenseDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.utils.AiCashFlowForecaster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiCashFlowUiState(
    val forecasts: List<AiCashFlowForecaster.CashFlowForecast> = emptyList(),
    val summary: AiCashFlowForecaster.CashFlowSummary? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class AiCashFlowViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiCashFlowUiState())
    val uiState: StateFlow<AiCashFlowUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    init {
        generateForecast()
    }

    fun generateForecast() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val expenses = expenseDao.getExpensesByCompany(companyId).first()

            val forecasts = AiCashFlowForecaster.forecastCashFlow(invoices, expenses, 6)
            val totalIncome = invoices.sumOf { it.grandTotal }
            val totalExpenses = expenses.sumOf { it.amount }
            val summary = AiCashFlowForecaster.generateSummary(expenses, totalIncome - totalExpenses)

            _uiState.value = AiCashFlowUiState(
                forecasts = forecasts,
                summary = summary,
                isLoading = false
            )
        }
    }
}

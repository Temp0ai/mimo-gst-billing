package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.ExpenseOptimizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseOptimizerUiState(
    val suggestions: List<ExpenseOptimizer.ExpenseSuggestion> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ExpenseOptimizerViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseOptimizerUiState())
    val uiState: StateFlow<ExpenseOptimizerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val expenses = expenseDao.getExpensesByCompany(companyId).first()
            _uiState.value = ExpenseOptimizerUiState(
                suggestions = ExpenseOptimizer.analyze(expenses),
                isLoading = false
            )
        }
    }
}
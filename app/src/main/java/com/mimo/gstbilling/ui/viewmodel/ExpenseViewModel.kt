package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ExpenseDao
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseDao: ExpenseDao
) : ViewModel() {
    private val companyId = 1L

    val expenses: StateFlow<List<ExpenseEntity>> = expenseDao.getExpensesByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    init { loadTotal() }

    private fun loadTotal() {
        viewModelScope.launch { _totalExpenses.value = expenseDao.getTotalExpenses(companyId) ?: 0.0 }
    }

    fun addExpense(category: String, amount: Double, date: Long, description: String?, paymentMode: String) {
        viewModelScope.launch {
            expenseDao.insertExpense(ExpenseEntity(companyId = companyId, category = category, amount = amount, date = date, description = description, paymentMode = paymentMode))
            loadTotal()
        }
    }

    fun editExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
            loadTotal()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.deleteExpense(expense)
            loadTotal()
        }
    }

    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesByCategoryFlow(companyId, category)

    fun getMonthlyTotal(month: Int, year: Int): Flow<Double> = flow {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        emit(expenseDao.getMonthlyTotal(companyId, start, end) ?: 0.0)
    }
}

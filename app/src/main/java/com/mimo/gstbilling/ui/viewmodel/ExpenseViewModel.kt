package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.ExpenseDao
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.utils.RecycleBinHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val companyDao: CompanyDao,
    private val recycleBinHelper: RecycleBinHelper
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    private val _expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = _expenses.asStateFlow()

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    init {
        loadExpenses()
        loadTotal()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            expenseDao.getExpensesByCompany(cId).collect { _expenses.value = it }
        }
    }

    private fun loadTotal() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            _totalExpenses.value = expenseDao.getTotalExpenses(cId) ?: 0.0
        }
    }

    fun addExpense(category: String, amount: Double, date: Long, description: String?, paymentMode: String) {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            expenseDao.insertExpense(ExpenseEntity(companyId = cId, category = category, amount = amount, date = date, description = description, paymentMode = paymentMode))
            loadExpenses()
            loadTotal()
        }
    }

    fun editExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
            loadExpenses()
            loadTotal()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            recycleBinHelper.deleteExpenseToBin(expense)
            loadExpenses()
            loadTotal()
        }
    }

    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> = flow {
        val cId = getCurrentCompanyId()
        emit(expenseDao.getExpensesByCategoryFlow(cId, category).first())
    }

    fun getMonthlyTotal(month: Int, year: Int): Flow<Double> = flow {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        val cId = getCurrentCompanyId()
        emit(expenseDao.getMonthlyTotal(cId, start, end) ?: 0.0)
    }
}

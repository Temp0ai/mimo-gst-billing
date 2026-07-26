package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.TransactionDao
import com.mimo.gstbilling.data.local.entity.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CashBankViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val invoiceDao: InvoiceDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    private val _totalCredit = MutableStateFlow(0.0)
    val totalCredit: StateFlow<Double> = _totalCredit.asStateFlow()

    private val _totalDebit = MutableStateFlow(0.0)
    val totalDebit: StateFlow<Double> = _totalDebit.asStateFlow()

    private val _cashBalance = MutableStateFlow(0.0)
    val cashBalance: StateFlow<Double> = _cashBalance.asStateFlow()

    init {
        loadTransactions()
        loadSummary()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            transactionDao.getTransactionsByCompany(cId).collect { _transactions.value = it }
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            _totalCredit.value = transactionDao.getTotalCredit(cId) ?: 0.0
            _totalDebit.value = transactionDao.getTotalDebit(cId) ?: 0.0
            _cashBalance.value = _totalCredit.value - _totalDebit.value
        }
    }

    fun addTransaction(
        partyId: Long,
        amount: Double,
        type: String,
        mode: String,
        description: String?
    ) {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            val transaction = TransactionEntity(
                companyId = cId,
                partyId = partyId,
                amount = amount,
                type = type,
                mode = mode,
                description = description,
                date = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(transaction)
            loadTransactions()
            loadSummary()
        }
    }
}

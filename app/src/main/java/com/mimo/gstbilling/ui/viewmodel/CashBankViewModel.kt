package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    private val companyId = 1L

    val transactions: StateFlow<List<TransactionEntity>> = transactionDao.getTransactionsByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalCredit = MutableStateFlow(0.0)
    val totalCredit: StateFlow<Double> = _totalCredit.asStateFlow()

    private val _totalDebit = MutableStateFlow(0.0)
    val totalDebit: StateFlow<Double> = _totalDebit.asStateFlow()

    private val _cashBalance = MutableStateFlow(0.0)
    val cashBalance: StateFlow<Double> = _cashBalance.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            _totalCredit.value = transactionDao.getTotalCredit(companyId) ?: 0.0
            _totalDebit.value = transactionDao.getTotalDebit(companyId) ?: 0.0
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
            val transaction = TransactionEntity(
                companyId = companyId,
                partyId = partyId,
                amount = amount,
                type = type,
                mode = mode,
                description = description,
                date = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(transaction)
            loadSummary()
        }
    }
}

package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import com.mimo.gstbilling.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiDashboardUiState(
    val insightsCount: Int = 0,
    val alertsCount: Int = 0,
    val suggestionsCount: Int = 0,
    val recentInsights: List<AiBusinessInsights.BusinessInsight> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AiDashboardViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao,
    private val expenseDao: ExpenseDao,
    private val itemDao: ItemDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiDashboardUiState())
    val uiState: StateFlow<AiDashboardUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L
    private var allInvoices: List<InvoiceEntity> = emptyList()
    private var allParties: List<PartyEntity> = emptyList()
    private var allExpenses: List<ExpenseEntity> = emptyList()

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().first()?.let { company ->
                companyId = company.id
                loadData()
            }
        }
    }

    private suspend fun loadData() {
        invoiceDao.getInvoicesByCompany(companyId).collect { invoices ->
            allInvoices = invoices
            partyDao.getPartiesByCompany(companyId).collect { parties ->
                allParties = parties
                expenseDao.getExpensesByCompany(companyId).collect { expenses ->
                    allExpenses = expenses
                    generateInsights()
                }
            }
        }
    }

    private fun generateInsights() {
        viewModelScope.launch {
            val insights = AiBusinessInsights.generateInsights(allInvoices, allExpenses, allParties)
            val anomalies = AiAnomalyDetector.generateReport(allInvoices, allExpenses, allParties)
            val reminders = AiSmartReminders.generateReminders(allInvoices, allParties)

            _uiState.value = AiDashboardUiState(
                insightsCount = insights.size,
                alertsCount = anomalies.anomalyCount,
                suggestionsCount = reminders.size,
                recentInsights = insights,
                isLoading = false
            )
        }
    }

    fun getInvoices() = allInvoices
    fun getParties() = allParties
    fun getExpenses() = allExpenses
}

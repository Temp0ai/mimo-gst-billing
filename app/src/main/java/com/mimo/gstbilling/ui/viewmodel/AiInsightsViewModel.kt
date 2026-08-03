package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.ExpenseDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import com.mimo.gstbilling.utils.AiBusinessInsights
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiInsightsUiState(
    val metrics: AiBusinessInsights.BusinessMetrics? = null,
    val topCustomers: List<AiBusinessInsights.TopCustomer> = emptyList(),
    val salesTrends: List<AiBusinessInsights.SalesTrend> = emptyList(),
    val insights: List<AiBusinessInsights.BusinessInsight> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AiInsightsViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao,
    private val expenseDao: ExpenseDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiInsightsUiState())
    val uiState: StateFlow<AiInsightsUiState> = _uiState.asStateFlow()

    private var allInvoices: List<InvoiceEntity> = emptyList()
    private var allParties: List<PartyEntity> = emptyList()
    private var allExpenses: List<ExpenseEntity> = emptyList()
    private var companyId: Long = 1L

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().first()?.let { company ->
                companyId = company.id
            }
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            invoiceDao.getInvoicesByCompany(companyId).collect { invoices ->
                allInvoices = invoices
                partyDao.getPartiesByCompany(companyId).collect { parties ->
                    allParties = parties
                    expenseDao.getExpensesByCompany(companyId).collect { expenses ->
                        allExpenses = expenses
                        refreshInsights()
                    }
                }
            }
        }
    }

    fun refreshInsights() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val metrics = AiBusinessInsights.calculateMetrics(allInvoices, allExpenses, allParties)
            val topCustomers = AiBusinessInsights.getTopCustomers(allInvoices, allParties, 10)
            val salesTrends = AiBusinessInsights.getSalesTrends(allInvoices, 6)
            val insights = AiBusinessInsights.generateInsights(allInvoices, allExpenses, allParties)

            _uiState.value = AiInsightsUiState(
                metrics = metrics,
                topCustomers = topCustomers,
                salesTrends = salesTrends,
                insights = insights,
                isLoading = false
            )
        }
    }
}

package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartyBalance(
    val party: PartyEntity,
    val balance: Double,
    val isReceivable: Boolean
)

data class DashboardData(
    val companyName: String = "My Business",
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val pendingReceivables: Double = 0.0,
    val pendingPayables: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalTax: Double = 0.0,
    val todaySales: Double = 0.0,
    val recentParties: List<PartyBalance> = emptyList(),
    val recentInvoices: List<InvoiceEntity> = emptyList(),
    val recentItems: List<ItemEntity> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val partyDao: PartyDao,
    private val itemDao: ItemDao,
    private val invoiceDao: InvoiceDao,
    private val expenseDao: ExpenseDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _data = MutableStateFlow(DashboardData())
    val data: StateFlow<DashboardData> = _data.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val companyId = 1L

    init {
        loadDashboardData()
    }

    fun refresh() {
        _isLoading.value = true
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                companyDao.getSelectedCompany(),
                partyDao.getPartiesByCompany(companyId),
                invoiceDao.getInvoicesByCompany(companyId),
                itemDao.getItemsByCompany(companyId)
            ) { company, parties, invoices, items ->
                val companyName = company?.name ?: "My Business"
                val totalSales = invoiceDao.getTotalSales(companyId) ?: 0.0
                val totalPurchases = invoiceDao.getTotalPurchases(companyId) ?: 0.0
                val pendingReceivables = invoiceDao.getPendingReceivables(companyId) ?: 0.0
                val pendingPayables = invoiceDao.getPendingPayables(companyId) ?: 0.0
                val totalExpenses = expenseDao.getTotalExpenses(companyId) ?: 0.0
                val totalTax = (invoiceDao.getTotalTax(companyId, "sales") ?: 0.0) +
                        (invoiceDao.getTotalTax(companyId, "purchase") ?: 0.0)

                val todayStart = getTodayStart()
                val todaySales = invoices.filter {
                    it.invoiceType == "sales" && it.invoiceDate >= todayStart
                }.sumOf { it.totalAmount }

                val partyBalances = parties.map { party ->
                    val partyInvoices = invoices.filter { it.partyId == party.id }
                    val receivable = partyInvoices
                        .filter { it.invoiceType == "sales" && it.paymentStatus != "paid" }
                        .sumOf { it.totalAmount - it.amountPaid }
                    val payable = partyInvoices
                        .filter { it.invoiceType == "purchase" && it.paymentStatus != "paid" }
                        .sumOf { it.totalAmount - it.amountPaid }
                    val balance = receivable - payable
                    PartyBalance(
                        party = party,
                        balance = balance,
                        isReceivable = balance >= 0
                    )
                }.sortedByDescending { kotlin.math.abs(it.balance) }

                val recentInvoices = invoices.take(5)
                val recentItems = items.take(10)

                DashboardData(
                    companyName = companyName,
                    totalSales = totalSales,
                    totalPurchases = totalPurchases,
                    pendingReceivables = pendingReceivables,
                    pendingPayables = pendingPayables,
                    totalExpenses = totalExpenses,
                    totalTax = totalTax,
                    todaySales = todaySales,
                    recentParties = partyBalances,
                    recentInvoices = recentInvoices,
                    recentItems = recentItems
                )
            }.collect {
                _data.value = it
                _isLoading.value = false
            }
        }
    }

    private fun getTodayStart(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

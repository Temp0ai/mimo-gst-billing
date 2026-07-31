package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class PartyBalance(
    val party: PartyEntity,
    val balance: Double,
    val isReceivable: Boolean
)

data class DashboardData(
    val companyName: String = "My Business",
    val companyEmail: String = "",
    val selectedCompanyId: Long = 1L,
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

    val allCompanies: Flow<List<CompanyEntity>> = companyDao.getAllCompanies()

    fun switchCompany(companyId: Long) {
        viewModelScope.launch {
            companyDao.clearSelectedCompany()
            companyDao.setSelectedCompany(companyId)
        }
    }

    init {
        loadDashboardData()
    }

    fun refresh() {
        _isLoading.value = true
        loadDashboardData()
    }

    fun deleteInvoice(invoiceId: Long) {
        viewModelScope.launch {
            invoiceDao.getInvoiceById(invoiceId)?.let { invoiceDao.deleteInvoice(it) }
            refresh()
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            itemDao.getItemById(itemId)?.let { itemDao.deleteItem(it) }
            refresh()
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            companyDao.getSelectedCompany().collect { company ->
                val cId = company?.id ?: 1L
                val companyName = company?.name ?: "My Business"
                val companyEmail = company?.email ?: ""

                val parties = partyDao.getPartiesByCompany(cId).first()
                val invoices = invoiceDao.getInvoicesByCompany(cId).first()
                val items = itemDao.getItemsByCompany(cId).first()

                val totalSales = invoiceDao.getTotalSales(cId) ?: 0.0
                val totalPurchases = invoiceDao.getTotalPurchases(cId) ?: 0.0
                val pendingReceivables = invoiceDao.getPendingReceivables(cId) ?: 0.0
                val pendingPayables = invoiceDao.getPendingPayables(cId) ?: 0.0
                val totalExpenses = expenseDao.getTotalExpenses(cId) ?: 0.0
                val totalTax = (invoiceDao.getTotalTax(cId, "sales") ?: 0.0) +
                        (invoiceDao.getTotalTax(cId, "purchase") ?: 0.0)

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

                _data.value = DashboardData(
                    companyName = companyName,
                    companyEmail = companyEmail,
                    selectedCompanyId = cId,
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

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

data class AiDuplicatesUiState(
    val allGroups: List<DuplicateGroup> = emptyList(),
    val filteredGroups: List<DuplicateGroup> = emptyList(),
    val selectedFilter: String = "all",
    val totalDuplicates: Int = 0,
    val invoiceCount: Int = 0,
    val partyCount: Int = 0,
    val expenseCount: Int = 0,
    val isScanning: Boolean = false,
    val scanComplete: Boolean = false
)

@HiltViewModel
class AiDuplicatesViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao,
    private val expenseDao: ExpenseDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiDuplicatesUiState())
    val uiState: StateFlow<AiDuplicatesUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().first()?.let { company ->
                companyId = company.id
            }
        }
    }

    fun scanForDuplicates() {
        _uiState.value = _uiState.value.copy(isScanning = true, scanComplete = false)
        viewModelScope.launch {
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val parties = partyDao.getPartiesByCompany(companyId).first()
            val expenses = expenseDao.getExpensesByCompany(companyId).first()

            val invoiceGroups = AiDuplicateDetector.detectDuplicateInvoices(invoices)
            val partyGroups = AiDuplicateDetector.detectDuplicateParties(parties)
            val expenseGroups = AiDuplicateDetector.detectDuplicateExpenses(expenses)

            val allGroups = invoiceGroups + partyGroups + expenseGroups

            val invoiceCount = allGroups.count { it.type == "invoice" }
            val partyCount = allGroups.count { it.type == "party" }
            val expenseCount = allGroups.count { it.type == "expense" }

            _uiState.value = AiDuplicatesUiState(
                allGroups = allGroups,
                filteredGroups = allGroups,
                selectedFilter = _uiState.value.selectedFilter,
                totalDuplicates = allGroups.size,
                invoiceCount = invoiceCount,
                partyCount = partyCount,
                expenseCount = expenseCount,
                isScanning = false,
                scanComplete = true
            )

            applyFilter(_uiState.value.selectedFilter)
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilter(filter)
    }

    private fun applyFilter(filter: String) {
        val groups = _uiState.value.allGroups
        val filtered = when (filter) {
            "invoice" -> groups.filter { it.type == "invoice" }
            "party" -> groups.filter { it.type == "party" }
            "expense" -> groups.filter { it.type == "expense" }
            else -> groups
        }
        _uiState.value = _uiState.value.copy(filteredGroups = filtered)
    }
}

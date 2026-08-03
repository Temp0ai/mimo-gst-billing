package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.utils.AiSmartReminders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiSmartRemindersUiState(
    val reminders: List<AiSmartReminders.SmartReminder> = emptyList(),
    val totalOverdueAmount: Double = 0.0,
    val reminderCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val selectedFilter: String = "All"
)

@HiltViewModel
class AiSmartRemindersViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiSmartRemindersUiState())
    val uiState: StateFlow<AiSmartRemindersUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().first()?.let { company ->
                companyId = company.id
            }
            generateReminders()
        }
    }

    fun generateReminders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val parties = partyDao.getPartiesByCompany(companyId).first()

            val reminders = AiSmartReminders.generateReminders(invoices, parties)
            val stats = AiSmartReminders.getReminderStats(reminders)
            val totalOverdue = reminders.sumOf { it.amount }

            _uiState.value = AiSmartRemindersUiState(
                reminders = reminders,
                totalOverdueAmount = totalOverdue,
                reminderCounts = stats,
                isLoading = false,
                selectedFilter = "All"
            )
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun getFilteredReminders(): List<AiSmartReminders.SmartReminder> {
        val filter = _uiState.value.selectedFilter
        return if (filter == "All") {
            _uiState.value.reminders
        } else {
            _uiState.value.reminders.filter { it.reminderType.equals(filter, ignoreCase = true) }
        }
    }
}

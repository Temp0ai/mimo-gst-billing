package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.SmartInvoiceSuggester
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmartInvoiceSuggestUiState(
    val suggestions: List<SmartInvoiceSuggester.ItemSuggestion> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SmartInvoiceSuggestViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val partyDao: PartyDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(SmartInvoiceSuggestUiState())
    val uiState: StateFlow<SmartInvoiceSuggestUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val parties = partyDao.getPartiesByCompany(companyId).first()
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val items = invoiceItemDao.getAllInvoiceItemsByCompany(companyId).first()
            val allSuggestions = parties.flatMap { party ->
                SmartInvoiceSuggester.suggest(party.id, parties, invoices, items).map { it.copy(partyName = party.name) }
            }
            _uiState.value = SmartInvoiceSuggestUiState(
                suggestions = allSuggestions.take(20),
                isLoading = false
            )
        }
    }
}
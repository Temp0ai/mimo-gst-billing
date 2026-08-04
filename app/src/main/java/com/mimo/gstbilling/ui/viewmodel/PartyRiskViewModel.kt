package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.PartyRiskScorer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartyRiskUiState(
    val scores: List<PartyRiskScorer.PartyRiskScore> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PartyRiskViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val partyDao: PartyDao,
    private val invoiceDao: InvoiceDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(PartyRiskUiState())
    val uiState: StateFlow<PartyRiskUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val parties = partyDao.getPartiesByCompany(companyId).first()
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            _uiState.value = PartyRiskUiState(
                scores = PartyRiskScorer.score(parties, invoices),
                isLoading = false
            )
        }
    }
}
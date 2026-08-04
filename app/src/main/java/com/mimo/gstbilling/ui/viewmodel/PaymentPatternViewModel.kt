package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.PaymentPatternAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentPatternUiState(
    val patterns: List<PaymentPatternAnalyzer.PaymentPattern> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PaymentPatternViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val partyDao: PartyDao,
    private val invoiceDao: InvoiceDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentPatternUiState())
    val uiState: StateFlow<PaymentPatternUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val parties = partyDao.getPartiesByCompany(companyId).first()
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            _uiState.value = PaymentPatternUiState(
                patterns = PaymentPatternAnalyzer.analyze(parties, invoices),
                isLoading = false
            )
        }
    }
}
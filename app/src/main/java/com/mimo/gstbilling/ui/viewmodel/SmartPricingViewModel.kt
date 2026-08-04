package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.SmartPricingAdvisor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmartPricingUiState(
    val suggestions: List<SmartPricingAdvisor.PricingSuggestion> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SmartPricingViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(SmartPricingUiState())
    val uiState: StateFlow<SmartPricingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val items = invoiceItemDao.getAllInvoiceItemsByCompany(companyId).first()
            _uiState.value = SmartPricingUiState(
                suggestions = SmartPricingAdvisor.analyze(invoices, items),
                isLoading = false
            )
        }
    }
}
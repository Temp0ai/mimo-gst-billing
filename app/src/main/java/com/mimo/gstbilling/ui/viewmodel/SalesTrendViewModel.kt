package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.SalesTrendPredictor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesTrendUiState(
    val forecasts: List<SalesTrendPredictor.SalesForecast> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SalesTrendViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(SalesTrendUiState())
    val uiState: StateFlow<SalesTrendUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            _uiState.value = SalesTrendUiState(
                forecasts = SalesTrendPredictor.predict(invoices),
                isLoading = false
            )
        }
    }
}
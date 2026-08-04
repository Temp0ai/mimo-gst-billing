package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.utils.InventoryReorderPredictor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryReorderUiState(
    val alerts: List<InventoryReorderPredictor.ReorderAlert> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class InventoryReorderViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val itemDao: ItemDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryReorderUiState())
    val uiState: StateFlow<InventoryReorderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val companyId = companyDao.getSelectedCompany().first()?.id ?: return@launch
            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val items = invoiceItemDao.getAllInvoiceItemsByCompany(companyId).first()
            val allItems = itemDao.getItemsByCompany(companyId).first()
            _uiState.value = InventoryReorderUiState(
                alerts = InventoryReorderPredictor.predict(invoices, items, allItems),
                isLoading = false
            )
        }
    }
}
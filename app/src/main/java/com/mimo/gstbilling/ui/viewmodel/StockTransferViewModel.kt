package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.StockTransferDao
import com.mimo.gstbilling.data.local.dao.StoreDao
import com.mimo.gstbilling.data.local.entity.StockTransferEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockTransferViewModel @Inject constructor(
    private val stockTransferDao: StockTransferDao,
    private val storeDao: StoreDao
) : ViewModel() {

    private val companyId = 1L

    val transfers: StateFlow<List<StockTransferEntity>> = stockTransferDao.getTransfersByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stores = storeDao.getStoresByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransfer(fromStoreId: Long, toStoreId: Long, itemName: String, qty: Double, unit: String, notes: String?) {
        viewModelScope.launch {
            stockTransferDao.insertTransfer(
                StockTransferEntity(
                    companyId = companyId, fromStoreId = fromStoreId, toStoreId = toStoreId,
                    itemName = itemName, quantity = qty, unit = unit, date = System.currentTimeMillis(), notes = notes
                )
            )
        }
    }

    fun deleteTransfer(transfer: StockTransferEntity) {
        viewModelScope.launch { stockTransferDao.deleteTransfer(transfer) }
    }
}

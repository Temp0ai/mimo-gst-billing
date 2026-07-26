package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.StockTransferDao
import com.mimo.gstbilling.data.local.dao.WarehouseDao
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.entity.StockTransferEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockTransferViewModel @Inject constructor(
    private val stockTransferDao: StockTransferDao,
    private val warehouseDao: WarehouseDao,
    private val itemDao: ItemDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val transfers: StateFlow<List<StockTransferEntity>> = companyDao.getSelectedCompany()
        .flatMapLatest { company ->
            stockTransferDao.getTransfersByCompany(company?.id ?: 1L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val warehouses = companyDao.getSelectedCompany()
        .flatMapLatest { company ->
            warehouseDao.getWarehousesByCompany(company?.id ?: 1L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items = companyDao.getSelectedCompany()
        .flatMapLatest { company ->
            itemDao.getItemsByCompany(company?.id ?: 1L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransfer(fromWarehouseId: Long, toWarehouseId: Long, itemId: Long, itemName: String, qty: Double, notes: String?) {
        viewModelScope.launch {
            val companyId = getCurrentCompanyId()
            stockTransferDao.insertTransfer(
                StockTransferEntity(
                    companyId = companyId,
                    fromWarehouseId = fromWarehouseId,
                    toWarehouseId = toWarehouseId,
                    itemId = itemId,
                    itemName = itemName,
                    quantity = qty,
                    transferDate = System.currentTimeMillis(),
                    notes = notes
                )
            )
        }
    }

    fun deleteTransfer(transfer: StockTransferEntity) {
        viewModelScope.launch { stockTransferDao.deleteTransfer(transfer) }
    }
}

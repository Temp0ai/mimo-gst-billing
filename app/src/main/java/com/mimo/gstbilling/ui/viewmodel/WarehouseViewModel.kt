package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.WarehouseDao
import com.mimo.gstbilling.data.local.entity.WarehouseEntity
import com.mimo.gstbilling.data.local.entity.StockTransferEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WarehouseViewModel @Inject constructor(
    private val warehouseDao: WarehouseDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val warehouses: StateFlow<List<WarehouseEntity>> = flow { emit(getCurrentCompanyId()) }
        .flatMapLatest { id -> warehouseDao.getWarehousesByCompany(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockTransfers: StateFlow<List<StockTransferEntity>> = flow { emit(getCurrentCompanyId()) }
        .flatMapLatest { id -> warehouseDao.getStockTransfers(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWarehouse(name: String, address: String?, phone: String?, managerName: String?) {
        viewModelScope.launch {
            val count = warehouses.value.size
            warehouseDao.insertWarehouse(WarehouseEntity(companyId = getCurrentCompanyId(), name = name, address = address, phone = phone, managerName = managerName, isDefault = count == 0))
        }
    }

    fun editWarehouse(warehouse: WarehouseEntity) {
        viewModelScope.launch { warehouseDao.updateWarehouse(warehouse) }
    }

    fun deleteWarehouse(warehouse: WarehouseEntity) {
        viewModelScope.launch { warehouseDao.deleteWarehouse(warehouse) }
    }

    fun setDefault(warehouse: WarehouseEntity) {
        viewModelScope.launch {
            warehouses.value.forEach { wh ->
                if (wh.isDefault && wh.id != warehouse.id) {
                    warehouseDao.updateWarehouse(wh.copy(isDefault = false))
                }
            }
            warehouseDao.updateWarehouse(warehouse.copy(isDefault = true))
        }
    }

    fun addTransfer(fromId: Long, toId: Long, itemId: Long, itemName: String, quantity: Double, notes: String?) {
        viewModelScope.launch {
            warehouseDao.insertStockTransfer(StockTransferEntity(companyId = getCurrentCompanyId(), fromWarehouseId = fromId, toWarehouseId = toId, itemId = itemId, itemName = itemName, quantity = quantity, transferDate = System.currentTimeMillis(), notes = notes))
        }
    }
}

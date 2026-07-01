package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.entity.ItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemDao: ItemDao
) : ViewModel() {

    private val companyId = 1L

    val allItems: StateFlow<List<ItemEntity>> = itemDao.getItemsByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ItemEntity>> = itemDao.getProductsByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ItemEntity>> = itemDao.getServicesByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<ItemEntity>> = itemDao.getLowStockItems(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()

    private val _lowStockCount = MutableStateFlow(0)
    val lowStockCount: StateFlow<Int> = _lowStockCount.asStateFlow()

    private val _serviceCount = MutableStateFlow(0)
    val serviceCount: StateFlow<Int> = _serviceCount.asStateFlow()

    init {
        loadCounts()
    }

    private fun loadCounts() {
        viewModelScope.launch {
            _itemCount.value = itemDao.getItemCount(companyId)
            _lowStockCount.value = itemDao.getLowStockCount(companyId)
            _serviceCount.value = itemDao.getServiceCount(companyId)
        }
    }

    fun addItem(
        name: String,
        hsnCode: String?,
        description: String?,
        salePrice: Double,
        purchasePrice: Double,
        gstRate: Double,
        unit: String,
        stockQuantity: Double,
        isService: Boolean
    ) {
        viewModelScope.launch {
            val item = ItemEntity(
                companyId = companyId,
                name = name,
                hsnCode = hsnCode?.ifBlank { null },
                description = description?.ifBlank { null },
                salePrice = salePrice,
                purchasePrice = purchasePrice,
                gstRate = gstRate,
                unit = unit,
                stockQuantity = stockQuantity,
                isService = isService
            )
            itemDao.insertItem(item)
            loadCounts()
        }
    }

    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.updateItem(item)
            loadCounts()
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.deleteItem(item)
            loadCounts()
        }
    }

    suspend fun getItemById(id: Long): ItemEntity? {
        return itemDao.getItemById(id)
    }
}

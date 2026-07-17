package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.dao.ItemVariantDao
import com.mimo.gstbilling.data.local.entity.ItemEntity
import com.mimo.gstbilling.data.local.entity.ItemVariantEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemDao: ItemDao,
    private val itemVariantDao: ItemVariantDao
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

    fun addItemWithVariants(
        name: String,
        hsnCode: String?,
        description: String?,
        salePrice: Double,
        purchasePrice: Double,
        gstRate: Double,
        unit: String,
        stockQuantity: Double,
        isService: Boolean,
        variants: List<Triple<String, Double, Double>>
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
            val itemId = itemDao.insertItem(item)
            variants.forEach { (variantName, variantSalePrice, variantStockQty) ->
                itemVariantDao.insertVariant(
                    ItemVariantEntity(
                        itemId = itemId,
                        variantName = variantName,
                        salePrice = variantSalePrice,
                        stockQuantity = variantStockQty,
                        unit = unit
                    )
                )
            }
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

    fun getVariantsByItem(itemId: Long): Flow<List<ItemVariantEntity>> {
        return itemVariantDao.getVariantsByItem(itemId)
    }

    fun addVariant(
        itemId: Long,
        variantName: String,
        salePrice: Double,
        purchasePrice: Double,
        stockQuantity: Double,
        unit: String,
        sku: String?,
        barcode: String?
    ) {
        viewModelScope.launch {
            val variant = ItemVariantEntity(
                itemId = itemId,
                variantName = variantName,
                salePrice = salePrice,
                purchasePrice = purchasePrice,
                stockQuantity = stockQuantity,
                unit = unit,
                sku = sku?.ifBlank { null },
                barcode = barcode?.ifBlank { null }
            )
            itemVariantDao.insertVariant(variant)
        }
    }

    fun updateVariant(variant: ItemVariantEntity) {
        viewModelScope.launch {
            itemVariantDao.updateVariant(variant)
        }
    }

    fun deleteVariant(variant: ItemVariantEntity) {
        viewModelScope.launch {
            itemVariantDao.deleteVariant(variant)
        }
    }
}

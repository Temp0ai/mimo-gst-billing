package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.dao.ItemVariantDao
import com.mimo.gstbilling.data.local.entity.ItemEntity
import com.mimo.gstbilling.data.local.entity.ItemVariantEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemDao: ItemDao,
    private val itemVariantDao: ItemVariantDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    private val _allItems = MutableStateFlow<List<ItemEntity>>(emptyList())
    val allItems: StateFlow<List<ItemEntity>> = _allItems.asStateFlow()

    private val _products = MutableStateFlow<List<ItemEntity>>(emptyList())
    val products: StateFlow<List<ItemEntity>> = _products.asStateFlow()

    private val _services = MutableStateFlow<List<ItemEntity>>(emptyList())
    val services: StateFlow<List<ItemEntity>> = _services.asStateFlow()

    private val _lowStockItems = MutableStateFlow<List<ItemEntity>>(emptyList())
    val lowStockItems: StateFlow<List<ItemEntity>> = _lowStockItems.asStateFlow()

    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()

    private val _lowStockCount = MutableStateFlow(0)
    val lowStockCount: StateFlow<Int> = _lowStockCount.asStateFlow()

    private val _serviceCount = MutableStateFlow(0)
    val serviceCount: StateFlow<Int> = _serviceCount.asStateFlow()

    init {
        viewModelScope.launch {
            _cachedCompanyId = getCurrentCompanyId()
            loadAllItems()
            loadCounts()
        }
    }

    private var _cachedCompanyId: Long = 1L

    private fun loadAllItems() {
        viewModelScope.launch {
            val cId = _cachedCompanyId
            combine(
                itemDao.getItemsByCompany(cId),
                itemDao.getProductsByCompany(cId),
                itemDao.getServicesByCompany(cId),
                itemDao.getLowStockItems(cId)
            ) { items, products, services, lowStock ->
                _allItems.value = items
                _products.value = products
                _services.value = services
                _lowStockItems.value = lowStock
            }.collect()
        }
    }

    private fun loadCounts() {
        viewModelScope.launch {
            val cId = _cachedCompanyId
            _itemCount.value = itemDao.getItemCount(cId)
            _lowStockCount.value = itemDao.getLowStockCount(cId)
            _serviceCount.value = itemDao.getServiceCount(cId)
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            _cachedCompanyId = getCurrentCompanyId()
            loadAllItems()
            loadCounts()
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
        isService: Boolean,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            val cId = _cachedCompanyId
            val item = ItemEntity(
                companyId = cId,
                name = name,
                hsnCode = hsnCode?.ifBlank { null },
                description = description?.ifBlank { null },
                salePrice = salePrice,
                purchasePrice = purchasePrice,
                gstRate = gstRate,
                unit = unit,
                stockQuantity = stockQuantity,
                isService = isService,
                imageUri = imageUri
            )
            itemDao.insertItem(item)
            loadCounts()
            loadAllItems()
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
        variants: List<Triple<String, Double, Double>>,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            val cId = _cachedCompanyId
            val item = ItemEntity(
                companyId = cId,
                name = name,
                hsnCode = hsnCode?.ifBlank { null },
                description = description?.ifBlank { null },
                salePrice = salePrice,
                purchasePrice = purchasePrice,
                gstRate = gstRate,
                unit = unit,
                stockQuantity = stockQuantity,
                isService = isService,
                imageUri = imageUri
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
            loadAllItems()
        }
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

    fun toggleItemActive(item: ItemEntity) {
        viewModelScope.launch {
            itemDao.updateItem(item.copy(isActive = !item.isActive))
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

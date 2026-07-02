package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ItemBatchDao
import com.mimo.gstbilling.data.local.entity.ItemBatchEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemBatchViewModel @Inject constructor(
    private val itemBatchDao: ItemBatchDao
) : ViewModel() {

    private val companyId = 1L

    val batches: StateFlow<List<ItemBatchEntity>> = itemBatchDao.getBatchesByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBatch(itemId: Long, itemName: String, batchNumber: String, serialNumber: String?, expiryDate: Long?, qty: Double, purchasePrice: Double, salePrice: Double, mfgDate: Long?) {
        viewModelScope.launch {
            itemBatchDao.insertBatch(
                ItemBatchEntity(
                    companyId = companyId, itemId = itemId, itemName = itemName,
                    batchNumber = batchNumber, serialNumber = serialNumber, expiryDate = expiryDate,
                    quantity = qty, purchasePrice = purchasePrice, salePrice = salePrice, manufacturingDate = mfgDate
                )
            )
        }
    }

    fun deleteBatch(batch: ItemBatchEntity) {
        viewModelScope.launch { itemBatchDao.deleteBatch(batch) }
    }
}

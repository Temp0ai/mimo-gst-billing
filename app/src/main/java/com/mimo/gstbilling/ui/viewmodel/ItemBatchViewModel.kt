package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ItemBatchDao
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.entity.ItemBatchEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemBatchViewModel @Inject constructor(
    private val batchDao: ItemBatchDao,
    private val itemDao: ItemDao
) : ViewModel() {
    private val companyId = 1L

    val batches: StateFlow<List<ItemBatchEntity>> = batchDao.getBatchesByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items = itemDao.getItemsByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBatch(itemId: Long, itemName: String, batchNumber: String, quantity: Double, mfgDate: Long?, expiryDate: Long?, purchasePrice: Double) {
        viewModelScope.launch {
            batchDao.insertBatch(ItemBatchEntity(companyId = companyId, itemId = itemId, itemName = itemName, batchNumber = batchNumber, quantity = quantity, manufacturingDate = mfgDate, expiryDate = expiryDate, purchasePrice = purchasePrice))
        }
    }

    fun deleteBatch(batch: ItemBatchEntity) {
        viewModelScope.launch { batchDao.deleteBatch(batch) }
    }
}

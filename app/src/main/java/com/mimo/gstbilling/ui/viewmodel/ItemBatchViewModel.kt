package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
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
    private val itemDao: ItemDao,
    private val companyDao: CompanyDao
) : ViewModel() {
    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val batches: StateFlow<List<ItemBatchEntity>> = companyDao.getSelectedCompany()
        .flatMapLatest { company ->
            batchDao.getBatchesByCompany(company?.id ?: 1L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items = companyDao.getSelectedCompany()
        .flatMapLatest { company ->
            itemDao.getItemsByCompany(company?.id ?: 1L)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBatch(itemId: Long, itemName: String, batchNumber: String, quantity: Double, mfgDate: Long?, expiryDate: Long?, purchasePrice: Double) {
        viewModelScope.launch {
            val companyId = getCurrentCompanyId()
            batchDao.insertBatch(ItemBatchEntity(companyId = companyId, itemId = itemId, itemName = itemName, batchNumber = batchNumber, quantity = quantity, manufacturingDate = mfgDate, expiryDate = expiryDate, purchasePrice = purchasePrice))
        }
    }

    fun deleteBatch(batch: ItemBatchEntity) {
        viewModelScope.launch { batchDao.deleteBatch(batch) }
    }
}

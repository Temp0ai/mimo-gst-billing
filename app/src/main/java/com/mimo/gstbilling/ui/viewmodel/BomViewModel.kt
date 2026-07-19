package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.BomDao
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.entity.BillOfMaterialsEntity
import com.mimo.gstbilling.data.local.entity.BomItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BomViewModel @Inject constructor(
    private val bomDao: BomDao,
    private val itemDao: ItemDao
) : ViewModel() {
    private val companyId = 1L

    val boms: StateFlow<List<BillOfMaterialsEntity>> = bomDao.getBomsByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items = itemDao.getItemsByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getBomItems(bomId: Long): Flow<List<BomItemEntity>> = bomDao.getBomItems(bomId)

    fun addBom(name: String, outputItemId: Long, outputItemName: String, outputQty: Double, notes: String?, bomItems: List<BomItemEntity>) {
        viewModelScope.launch {
            val bomId = bomDao.insertBom(BillOfMaterialsEntity(companyId = companyId, name = name, outputItemId = outputItemId, outputItemName = outputItemName, outputQuantity = outputQty, notes = notes))
            val items = bomItems.map { it.copy(bomId = bomId) }
            bomDao.insertBomItems(items)
        }
    }

    fun deleteBom(bom: BillOfMaterialsEntity) {
        viewModelScope.launch {
            bomDao.deleteAllBomItems(bom.id)
            bomDao.deleteBom(bom)
        }
    }
}

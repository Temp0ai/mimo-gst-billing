package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ManufacturingDao
import com.mimo.gstbilling.data.local.entity.ManufacturingEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManufacturingViewModel @Inject constructor(
    private val manufacturingDao: ManufacturingDao
) : ViewModel() {

    private val companyId = 1L

    val entries: StateFlow<List<ManufacturingEntity>> = manufacturingDao.getManufacturingByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalCost = MutableStateFlow(0.0)
    val totalCost: StateFlow<Double> = _totalCost.asStateFlow()

    init { loadTotal() }

    private fun loadTotal() {
        viewModelScope.launch { _totalCost.value = manufacturingDao.getTotalManufacturingCost(companyId) ?: 0.0 }
    }

    fun addEntry(name: String, qty: Double, rawMaterials: String, cost: Double, notes: String?) {
        viewModelScope.launch {
            manufacturingDao.insertManufacturing(
                ManufacturingEntity(
                    companyId = companyId,
                    finishedItemName = name,
                    finishedItemQty = qty,
                    rawMaterials = rawMaterials,
                    totalCost = cost,
                    date = System.currentTimeMillis(),
                    notes = notes
                )
            )
            loadTotal()
        }
    }

    fun deleteEntry(entry: ManufacturingEntity) {
        viewModelScope.launch { manufacturingDao.deleteManufacturing(entry); loadTotal() }
    }
}

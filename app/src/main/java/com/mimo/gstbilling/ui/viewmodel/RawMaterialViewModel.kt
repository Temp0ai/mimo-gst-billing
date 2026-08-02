package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.RawMaterialDao
import com.mimo.gstbilling.data.local.entity.RawMaterialEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RawMaterialUiState(
    val materials: List<RawMaterialEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RawMaterialViewModel @Inject constructor(
    private val rawMaterialDao: RawMaterialDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RawMaterialUiState())
    val uiState: StateFlow<RawMaterialUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().first()?.let { company ->
                companyId = company.id
                rawMaterialDao.getRawMaterialsByCompany(companyId).collect { materials ->
                    _uiState.value = RawMaterialUiState(materials = materials, isLoading = false)
                }
            }
        }
    }

    fun addMaterial(name: String, unit: String, stockQty: Double, costPerUnit: Double, hsnCode: String?) {
        viewModelScope.launch {
            rawMaterialDao.insertRawMaterial(
                RawMaterialEntity(
                    companyId = companyId,
                    name = name,
                    unit = unit,
                    stockQty = stockQty,
                    costPerUnit = costPerUnit,
                    hsnCode = hsnCode
                )
            )
        }
    }

    fun updateMaterial(material: RawMaterialEntity) {
        viewModelScope.launch {
            rawMaterialDao.updateRawMaterial(material)
        }
    }

    fun deleteMaterial(material: RawMaterialEntity) {
        viewModelScope.launch {
            rawMaterialDao.deleteRawMaterial(material)
        }
    }
}

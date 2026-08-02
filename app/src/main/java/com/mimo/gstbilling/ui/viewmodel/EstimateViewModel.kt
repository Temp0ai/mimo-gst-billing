package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.EstimateDao
import com.mimo.gstbilling.data.local.entity.EstimateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EstimateUiState(
    val estimates: List<EstimateEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class EstimateViewModel @Inject constructor(
    private val estimateDao: EstimateDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstimateUiState())
    val uiState: StateFlow<EstimateUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().first()?.let { company ->
                companyId = company.id
                estimateDao.getEstimatesByCompany(companyId).collect { estimates ->
                    _uiState.value = EstimateUiState(estimates = estimates, isLoading = false)
                }
            }
        }
    }

    fun addEstimate(estimateNumber: String, partyId: Long, partyName: String, amount: Double, date: Long, validUntil: Long, notes: String?) {
        viewModelScope.launch {
            estimateDao.insertEstimate(
                EstimateEntity(
                    companyId = companyId,
                    estimateNumber = estimateNumber,
                    partyId = partyId,
                    partyName = partyName,
                    amount = amount,
                    date = date,
                    validUntil = validUntil,
                    notes = notes
                )
            )
        }
    }

    fun updateEstimateStatus(id: Long, status: String) {
        viewModelScope.launch {
            estimateDao.updateEstimateStatus(id, status)
        }
    }

    fun deleteEstimate(estimate: EstimateEntity) {
        viewModelScope.launch {
            estimateDao.deleteEstimate(estimate)
        }
    }
}

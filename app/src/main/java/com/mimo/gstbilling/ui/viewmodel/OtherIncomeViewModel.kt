package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.OtherIncomeDao
import com.mimo.gstbilling.data.local.entity.OtherIncomeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtherIncomeUiState(
    val incomeList: List<OtherIncomeEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class OtherIncomeViewModel @Inject constructor(
    private val otherIncomeDao: OtherIncomeDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtherIncomeUiState())
    val uiState: StateFlow<OtherIncomeUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().first()?.let { company ->
                companyId = company.id
                otherIncomeDao.getOtherIncomeByCompany(companyId).collect { incomeList ->
                    _uiState.value = OtherIncomeUiState(
                        incomeList = incomeList,
                        totalIncome = incomeList.sumOf { it.amount },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addIncome(source: String, amount: Double, date: Long, description: String?, referenceNumber: String?) {
        viewModelScope.launch {
            otherIncomeDao.insertOtherIncome(
                OtherIncomeEntity(
                    companyId = companyId,
                    source = source,
                    amount = amount,
                    date = date,
                    description = description,
                    referenceNumber = referenceNumber
                )
            )
        }
    }

    fun deleteIncome(income: OtherIncomeEntity) {
        viewModelScope.launch {
            otherIncomeDao.deleteOtherIncome(income)
        }
    }
}

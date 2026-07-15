package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanySwitchViewModel @Inject constructor(
    private val companyDao: CompanyDao
) : ViewModel() {

    val companies = companyDao.getAllCompanies().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCompanyId = MutableStateFlow(0L)
    val selectedCompanyId: StateFlow<Long> = _selectedCompanyId.asStateFlow()

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().filterNotNull().collect { company ->
                _selectedCompanyId.value = company.id
            }
        }
    }

    fun switchCompany(companyId: Long) {
        viewModelScope.launch {
            companyDao.clearSelectedCompany()
            companyDao.setSelectedCompany(companyId)
            _selectedCompanyId.value = companyId
        }
    }

    fun addCompany(name: String, gstin: String?) {
        viewModelScope.launch {
            companyDao.insertCompany(CompanyEntity(name = name, gstin = gstin, address = null, phone = null, email = null, businessType = null, state = null, stateCode = null, logoUri = null, signatureUri = null, bankName = null, bankAccountNumber = null, bankIfsc = null, bankBranch = null, bankUpiId = null, termsAndConditions = null, declaration = null, msmeUdyamNumber = null))
        }
    }
}

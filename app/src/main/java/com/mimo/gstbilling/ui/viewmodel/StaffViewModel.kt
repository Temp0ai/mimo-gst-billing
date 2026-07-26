package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.StaffDao
import com.mimo.gstbilling.data.local.entity.StaffEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffDao: StaffDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val staff: StateFlow<List<StaffEntity>> = flow { emit(getCurrentCompanyId()) }
        .flatMapLatest { id -> staffDao.getStaffByCompany(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addStaff(name: String, phone: String?, email: String?, role: String) {
        viewModelScope.launch {
            staffDao.insertStaff(StaffEntity(companyId = getCurrentCompanyId(), name = name, phone = phone, email = email, role = role))
        }
    }

    fun editStaff(staffMember: StaffEntity) {
        viewModelScope.launch { staffDao.updateStaff(staffMember) }
    }

    fun deleteStaff(staffMember: StaffEntity) {
        viewModelScope.launch { staffDao.deleteStaff(staffMember) }
    }

    fun toggleActive(staffMember: StaffEntity) {
        viewModelScope.launch { staffDao.updateStaff(staffMember.copy(isActive = !staffMember.isActive)) }
    }
}

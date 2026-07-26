package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.DiscountConfigDao
import com.mimo.gstbilling.data.local.entity.DiscountConfigEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscountConfigViewModel @Inject constructor(
    private val discountDao: DiscountConfigDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    val discounts: StateFlow<List<DiscountConfigEntity>> = flow { emit(getCurrentCompanyId()) }
        .flatMapLatest { id -> discountDao.getDiscountsByCompany(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDiscount(name: String, type: String, value: Double, valueType: String, itemId: Long?, partyId: Long?) {
        viewModelScope.launch {
            discountDao.insertDiscount(DiscountConfigEntity(companyId = getCurrentCompanyId(), name = name, type = type, value = value, valueType = valueType, itemId = itemId, partyId = partyId))
        }
    }

    fun toggleActive(discount: DiscountConfigEntity) {
        viewModelScope.launch { discountDao.updateDiscount(discount.copy(isActive = !discount.isActive)) }
    }

    fun deleteDiscount(discount: DiscountConfigEntity) {
        viewModelScope.launch { discountDao.deleteDiscount(discount) }
    }
}

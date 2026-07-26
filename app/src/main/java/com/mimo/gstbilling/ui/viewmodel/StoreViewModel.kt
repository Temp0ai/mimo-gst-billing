package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.StoreDao
import com.mimo.gstbilling.data.local.entity.StoreEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeDao: StoreDao,
    private val companyDao: CompanyDao
) : ViewModel() {

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    private val _stores = MutableStateFlow<List<StoreEntity>>(emptyList())
    val stores: StateFlow<List<StoreEntity>> = _stores.asStateFlow()

    private val _storeCount = MutableStateFlow(0)
    val storeCount: StateFlow<Int> = _storeCount.asStateFlow()

    init {
        loadStores()
        loadCount()
    }

    private fun loadStores() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            storeDao.getStoresByCompany(cId).collect { _stores.value = it }
        }
    }

    private fun loadCount() {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            _storeCount.value = storeDao.getStoreCount(cId)
        }
    }

    fun addStore(name: String, address: String?, phone: String?) {
        viewModelScope.launch {
            val cId = getCurrentCompanyId()
            storeDao.insertStore(StoreEntity(companyId = cId, name = name, address = address, phone = phone))
            loadStores()
            loadCount()
        }
    }

    fun deleteStore(store: StoreEntity) {
        viewModelScope.launch {
            storeDao.deleteStore(store)
            loadStores()
            loadCount()
        }
    }
}

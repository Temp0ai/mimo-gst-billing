package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.StoreDao
import com.mimo.gstbilling.data.local.entity.StoreEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeDao: StoreDao
) : ViewModel() {

    private val companyId = 1L

    val stores: StateFlow<List<StoreEntity>> = storeDao.getStoresByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _storeCount = MutableStateFlow(0)
    val storeCount: StateFlow<Int> = _storeCount.asStateFlow()

    init { loadCount() }

    private fun loadCount() {
        viewModelScope.launch { _storeCount.value = storeDao.getStoreCount(companyId) }
    }

    fun addStore(name: String, address: String?, phone: String?) {
        viewModelScope.launch {
            storeDao.insertStore(StoreEntity(companyId = companyId, name = name, address = address, phone = phone))
            loadCount()
        }
    }

    fun deleteStore(store: StoreEntity) {
        viewModelScope.launch { storeDao.deleteStore(store); loadCount() }
    }
}

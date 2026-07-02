package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.PartyGroupDao
import com.mimo.gstbilling.data.local.entity.PartyGroupEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartyGroupViewModel @Inject constructor(
    private val partyGroupDao: PartyGroupDao
) : ViewModel() {

    private val companyId = 1L

    val groups: StateFlow<List<PartyGroupEntity>> = partyGroupDao.getGroupsByCompany(companyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGroup(name: String, description: String?) {
        viewModelScope.launch {
            partyGroupDao.insertGroup(PartyGroupEntity(companyId = companyId, name = name, description = description))
        }
    }

    fun deleteGroup(group: PartyGroupEntity) {
        viewModelScope.launch { partyGroupDao.deleteGroup(group) }
    }
}

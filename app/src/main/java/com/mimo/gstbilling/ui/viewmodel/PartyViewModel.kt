package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.entity.PartyEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val partyDao: PartyDao
) : ViewModel() {

    private val _parties = MutableStateFlow<List<PartyEntity>>(emptyList())
    val parties: StateFlow<List<PartyEntity>> = _parties

    private val _currentParty = MutableStateFlow<PartyEntity?>(null)
    val currentParty: StateFlow<PartyEntity?> = _currentParty

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadParties(companyId: Long) {
        viewModelScope.launch {
            partyDao.getPartiesByCompany(companyId).collect { list ->
                _parties.value = list
            }
        }
    }

    fun getPartyById(partyId: Long) {
        viewModelScope.launch {
            _currentParty.value = partyDao.getPartyById(partyId)
        }
    }

    fun addParty(
        companyId: Long,
        name: String,
        phone: String?,
        email: String?,
        gstin: String?,
        address: String?,
        state: String?,
        partyType: String
    ) {
        viewModelScope.launch {
            val party = PartyEntity(
                companyId = companyId,
                name = name,
                phone = phone,
                email = email,
                gstin = gstin,
                address = address,
                state = state,
                stateCode = null,
                balance = 0.0,
                partyType = partyType
            )
            partyDao.insertParty(party)
            _saveSuccess.value = true
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}

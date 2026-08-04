package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mimo.gstbilling.utils.GstFilingReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class GstFilingUiState(
    val deadlines: List<GstFilingReminder.FilingDeadline> = GstFilingReminder.getUpcoming(),
    val isLoading: Boolean = false
)

@HiltViewModel
class GstFilingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(GstFilingUiState())
    val uiState: StateFlow<GstFilingUiState> = _uiState
}
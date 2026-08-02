package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.ExpenseDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.entity.ExpenseEntity
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.PartyEntity
import com.mimo.gstbilling.utils.AiAnomalyDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiAnomaliesUiState(
    val anomalies: List<AiAnomalyDetector.Anomaly> = emptyList(),
    val riskScore: Float = 0f,
    val highCount: Int = 0,
    val mediumCount: Int = 0,
    val lowCount: Int = 0,
    val isScanning: Boolean = false
)

@HiltViewModel
class AiAnomaliesViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAnomaliesUiState())
    val uiState: StateFlow<AiAnomaliesUiState> = _uiState.asStateFlow()

    private var companyId: Long = 1L

    fun runDetection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)

            val invoices = invoiceDao.getInvoicesByCompany(companyId).first()
            val parties = partyDao.getPartiesByCompany(companyId).first()
            val expenses = expenseDao.getExpensesByCompany(companyId).first()

            val report = AiAnomalyDetector.generateReport(invoices, expenses, parties)

            _uiState.value = AiAnomaliesUiState(
                anomalies = report.anomalies,
                riskScore = report.riskScore,
                highCount = report.anomalies.count { it.severity == "high" },
                mediumCount = report.anomalies.count { it.severity == "medium" },
                lowCount = report.anomalies.count { it.severity == "low" },
                isScanning = false
            )
        }
    }
}

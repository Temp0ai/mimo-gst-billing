package com.mimo.gstbilling.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import com.mimo.gstbilling.utils.BankStatementParser
import com.mimo.gstbilling.utils.BankTxn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LedgerData(
    val entries: List<LedgerEntryEntity> = emptyList(),
    val bankEntries: List<LedgerEntryEntity> = emptyList(),
    val unreconciledApp: List<LedgerEntryEntity> = emptyList(),
    val unreconciledBank: List<LedgerEntryEntity> = emptyList(),
    val totalDebit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val balance: Double = 0.0
)

data class MatchResult(
    val appEntry: LedgerEntryEntity,
    val bankEntry: LedgerEntryEntity?,
    val score: Double = 0.0
)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val companyDao: CompanyDao,
    private val ledgerDao: LedgerDao,
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _companyId = MutableStateFlow(1L)
    private val _selectedPartyId = MutableStateFlow(0L)
    private val _ledgerData = MutableStateFlow(LedgerData())
    val ledgerData: StateFlow<LedgerData> = _ledgerData.asStateFlow()

    private val _matchResults = MutableStateFlow<List<MatchResult>>(emptyList())
    val matchResults: StateFlow<List<MatchResult>> = _matchResults.asStateFlow()

    private val _importedTxns = MutableStateFlow<List<BankTxn>>(emptyList())
    val importedTxns: StateFlow<List<BankTxn>> = _importedTxns.asStateFlow()

    private suspend fun getCurrentCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().collect { company ->
                _companyId.value = company?.id ?: 1L
                loadLedger()
            }
        }
    }

    fun selectParty(partyId: Long) {
        _selectedPartyId.value = partyId
        viewModelScope.launch { loadLedger() }
    }

    private suspend fun loadLedger() {
        val cId = _companyId.value
        val partyId = _selectedPartyId.value

        val entries = if (partyId > 0) {
            ledgerDao.getLedgerByParty(cId, partyId).first()
        } else {
            ledgerDao.getLedgerByCompany(cId).first()
        }

        val bankEntries = ledgerDao.getBankStatementEntries(cId).first()
        val unreconciledApp = ledgerDao.getUnreconciledAppEntries(cId).first()
        val unreconciledBank = ledgerDao.getUnreconciledBankEntries(cId).first()

        val totalDebit = entries.sumOf { it.debit }
        val totalCredit = entries.sumOf { it.credit }

        _ledgerData.value = LedgerData(
            entries = entries,
            bankEntries = bankEntries,
            unreconciledApp = unreconciledApp,
            unreconciledBank = unreconciledBank,
            totalDebit = totalDebit,
            totalCredit = totalCredit,
            balance = totalDebit - totalCredit
        )

        performMatching()
    }

    fun importBankStatement(context: Context, uri: Uri) {
        viewModelScope.launch {
            val cId = _companyId.value
            val txns = if (uri.toString().endsWith(".csv")) {
                BankStatementParser.parseCsv(context, uri)
            } else {
                BankStatementParser.parsePdf(context, uri)
            }
            _importedTxns.value = txns

            ledgerDao.deleteBankStatementEntries(cId)
            val entries = BankStatementParser.convertToLedgerEntries(txns, cId)
            ledgerDao.insertEntries(entries)
            loadLedger()
        }
    }

    private fun performMatching() {
        viewModelScope.launch {
            val appEntries = _ledgerData.value.unreconciledApp
            val bankEntries = _ledgerData.value.unreconciledBank
            val results = BankStatementParser.matchTransactions(appEntries, bankEntries)
            _matchResults.value = results.map { (app, bank) ->
                MatchResult(
                    appEntry = app,
                    bankEntry = bank,
                    score = if (bank != null) calculateMatchScore(app, bank) else 0.0
                )
            }
        }
    }

    private fun calculateMatchScore(a: LedgerEntryEntity, b: LedgerEntryEntity): Double {
        var score = 0.0
        if (a.debit > 0 && b.debit > 0 && kotlin.math.abs(a.debit - b.debit) < 1) score += 40.0
        if (a.credit > 0 && b.credit > 0 && kotlin.math.abs(a.credit - b.credit) < 1) score += 40.0
        if (kotlin.math.abs(a.date - b.date) < 24 * 60 * 60 * 1000) score += 20.0
        return score
    }

    fun reconcileEntries(appEntryId: Long, bankEntryId: Long) {
        viewModelScope.launch {
            ledgerDao.markReconciled(appEntryId, bankEntryId)
            ledgerDao.markReconciled(bankEntryId, appEntryId)
            loadLedger()
        }
    }

    fun reconcileAllMatched() {
        viewModelScope.launch {
            _matchResults.value.filter { it.bankEntry != null && it.score >= 60 }.forEach { result ->
                ledgerDao.markReconciled(result.appEntry.id, result.bankEntry!!.id)
                ledgerDao.markReconciled(result.bankEntry.id, result.appEntry.id)
            }
            loadLedger()
        }
    }

    fun generateGstr1Json(month: Int, year: Int): String {
        // Will be populated with invoice data
        return "{}"
    }
}

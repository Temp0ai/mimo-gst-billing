package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.LedgerEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries WHERE companyId = :companyId ORDER BY date ASC")
    fun getLedgerByCompany(companyId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE companyId = :companyId AND partyId = :partyId ORDER BY date ASC")
    fun getLedgerByParty(companyId: Long, partyId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE companyId = :companyId AND source = 'bank_statement' AND isReconciled = 0 ORDER BY date ASC")
    fun getUnreconciledBankEntries(companyId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE companyId = :companyId AND isReconciled = 1 ORDER BY date ASC")
    fun getReconciledEntries(companyId: Long): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE companyId = :companyId AND source = 'app' AND isReconciled = 0 AND (debit > 0 OR credit > 0) ORDER BY date ASC")
    fun getUnreconciledAppEntries(companyId: Long): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LedgerEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<LedgerEntryEntity>)

    @Update
    suspend fun updateEntry(entry: LedgerEntryEntity)

    @Query("UPDATE ledger_entries SET isReconciled = 1, reconciledWithId = :reconciledWithId WHERE id = :entryId")
    suspend fun markReconciled(entryId: Long, reconciledWithId: Long)

    @Query("UPDATE ledger_entries SET isReconciled = 0, reconciledWithId = 0 WHERE id = :entryId")
    suspend fun markUnreconciled(entryId: Long)

    @Query("SELECT SUM(debit) FROM ledger_entries WHERE companyId = :companyId AND partyId = :partyId")
    suspend fun getTotalDebit(companyId: Long, partyId: Long): Double?

    @Query("SELECT SUM(credit) FROM ledger_entries WHERE companyId = :companyId AND partyId = :partyId")
    suspend fun getTotalCredit(companyId: Long, partyId: Long): Double?

    @Query("DELETE FROM ledger_entries WHERE companyId = :companyId AND source = 'bank_statement'")
    suspend fun deleteBankStatementEntries(companyId: Long)

    @Query("SELECT * FROM ledger_entries WHERE companyId = :companyId AND source = 'bank_statement' ORDER BY date ASC")
    fun getBankStatementEntries(companyId: Long): Flow<List<LedgerEntryEntity>>
}

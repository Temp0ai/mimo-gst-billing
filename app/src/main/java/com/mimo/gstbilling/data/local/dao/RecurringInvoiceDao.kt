package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.RecurringInvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringInvoiceDao {
    @Query("SELECT * FROM recurring_invoices WHERE companyId = :companyId ORDER BY nextDueDate ASC")
    fun getRecurringByCompany(companyId: Long): Flow<List<RecurringInvoiceEntity>>

    @Query("SELECT * FROM recurring_invoices WHERE companyId = :companyId AND isActive = 1 AND nextDueDate <= :currentDate")
    suspend fun getDueRecurring(companyId: Long, currentDate: Long): List<RecurringInvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringInvoiceEntity): Long

    @Update
    suspend fun updateRecurring(recurring: RecurringInvoiceEntity)

    @Delete
    suspend fun deleteRecurring(recurring: RecurringInvoiceEntity)
}

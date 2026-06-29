package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.InvoiceEntity
import kotlinx.coroutines crushers.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE companyId = :companyId ORDER BY invoiceDate DESC")
    fun getInvoicesByCompany(companyId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE companyId = :companyId AND invoiceType = :type ORDER BY invoiceDate DESC")
    fun getInvoicesByType(companyId: Long, type: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceById(invoiceId: Long): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("SELECT COUNT(*) FROM invoices WHERE companyId = :companyId AND invoiceType = 'sales'")
    suspend fun getSalesInvoiceCount(companyId: Long): Int

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = 'sales'")
    suspend fun getTotalSales(companyId: Long): Double?
}

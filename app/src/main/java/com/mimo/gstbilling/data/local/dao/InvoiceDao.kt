package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT COUNT(*) FROM invoices WHERE companyId = :companyId AND invoiceType = 'purchase'")
    suspend fun getPurchaseInvoiceCount(companyId: Long): Int

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = 'sales'")
    suspend fun getTotalSales(companyId: Long): Double?

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = 'purchase'")
    suspend fun getTotalPurchases(companyId: Long): Double?

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = 'sales' AND paymentStatus = 'unpaid'")
    suspend fun getPendingReceivables(companyId: Long): Double?

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = 'purchase' AND paymentStatus = 'unpaid'")
    suspend fun getPendingPayables(companyId: Long): Double?

    @Query("SELECT SUM(amountPaid) FROM invoices WHERE companyId = :companyId AND invoiceType = 'sales'")
    suspend fun getCollectedAmount(companyId: Long): Double?

    @Query("SELECT SUM(amountPaid) FROM invoices WHERE companyId = :companyId AND invoiceType = 'purchase'")
    suspend fun getPaidAmount(companyId: Long): Double?

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = 'sales' AND paymentStatus = 'paid'")
    suspend fun getCollectedTotal(companyId: Long): Double?

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = 'purchase' AND paymentStatus = 'paid'")
    suspend fun getPaidTotal(companyId: Long): Double?

    @Query("SELECT SUM(cgstTotal + sgstTotal + igstTotal) FROM invoices WHERE companyId = :companyId AND invoiceType = :type")
    suspend fun getTotalTax(companyId: Long, type: String): Double?

    @Query("SELECT SUM(subTotal) FROM invoices WHERE companyId = :companyId AND invoiceType = :type")
    suspend fun getTotalSubtotal(companyId: Long, type: String): Double?

    @Query("SELECT SUM(taxableAmount) FROM invoices WHERE companyId = :companyId AND invoiceType = :type")
    suspend fun getTotalTaxable(companyId: Long, type: String): Double?

    @Query("SELECT SUM(cgstTotal) FROM invoices WHERE companyId = :companyId AND invoiceType = :type")
    suspend fun getTotalCgst(companyId: Long, type: String): Double?

    @Query("SELECT SUM(sgstTotal) FROM invoices WHERE companyId = :companyId AND invoiceType = :type")
    suspend fun getTotalSgst(companyId: Long, type: String): Double?

    @Query("SELECT SUM(igstTotal) FROM invoices WHERE companyId = :companyId AND invoiceType = :type")
    suspend fun getTotalIgst(companyId: Long, type: String): Double?
}

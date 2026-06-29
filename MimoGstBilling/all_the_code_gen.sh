#!/bin/bash
PROJECT="/root/Downloads/mimo ai/MimoGstBilling/app/src/main/java/com/mimo/gstbilling"

# InvoiceItemEntity
cat > "$PROJECT/data/local/entity/InvoiceItemEntity.kt" << 'KTOFF'
package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "invoice_items",
    primaryKeys = ["invoiceId", "itemId"],
    foreignKeys = [
        ForeignKey(entity = InvoiceEntity::class, parentColumns = ["id"], childColumns = ["invoiceId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItemEntity(
    val invoiceId: Long,
    val itemId: Long,
    val itemName: String,
    val hsnCode: String?,
    val quantity: Double,
    val unit: String,
    val price: Double,
    val discount: Double = 0.0,
    val gstRate: Double,
    val taxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double
)
KTOFF

# TransactionEntity
cat > "$PROJECT/data/local/entity/TransactionEntity.kt" << 'KTOFF'
package com.mimo.gstbilling.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transactions", indices = [Index(value = ["companyId"]), Index(value = ["partyId"])])
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyId: Long,
    val partyId: Long,
    val invoiceId: Long? = null,
    val amount: Double,
    val type: String, // credit, debit
    val mode: String, // cash, bank, upi, cheque
    val description: String?,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
KTOFF

# CompanyDao
cat > "$PROJECT/data/local/dao/CompanyDao.kt" << 'KTOFF'
package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE isSelected = 1 LIMIT 1")
    fun getSelectedCompany(): Flow<CompanyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity): Long

    @Update
    suspend fun updateCompany(company: CompanyEntity)

    @Delete
    suspend fun deleteCompany(company: CompanyEntity)

    @Query("UPDATE companies SET isSelected = 0")
    suspend fun clearSelectedCompany()

    @Query("UPDATE companies SET isSelected = 1 WHERE id = :companyId")
    suspend fun setSelectedCompany(companyId: Long)
}
KTOFF

# PartyDao
cat > "$PROJECT/data/local/dao/PartyDao.kt" << 'KTOFF'
package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties WHERE companyId = :companyId ORDER BY name ASC")
    fun getPartiesByCompany(companyId: Long): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE companyId = :companyId AND partyType = :type ORDER BY name ASC")
    fun getPartiesByType(companyId: Long, type: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :partyId")
    suspend fun getPartyById(partyId: Long): PartyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(partyEntity: PartyEntity): Long

    @Update
    suspend fun updateParty(partyEntity: PartyEntity)

    @Delete
    suspend fun deleteParty(partyEntity: PartyEntity)

    @Query("SELECT COUNT(*) FROM parties WHERE companyId = :companyId")
    suspend fun getPartyCount(companyId: Long): Int
}
KTOFF

# ItemDao
cat > "$PROJECT/data/local/dao/ItemDao.kt" << 'KTOFF'
package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE companyId = :companyId ORDER BY name ASC")
    fun getItemsByCompany(companyId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("SELECT COUNT(*) FROM items WHERE companyId = :companyId")
    suspend fun getItemCount(companyId: Long): Int
}
KTOFF

# InvoiceDao
cat > "$PROJECT/data/local/dao/InvoiceDao.kt" << 'KTOFF'
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
KTOFF

# InvoiceItemDao
cat > "$PROJECT/data/local/dao/InvoiceItemDao.kt" << 'KTOFF'
package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.InvoiceItemEntity

@Dao
interface InvoiceItemDao {
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InvoiceItemEntity>)
}
KTOFF


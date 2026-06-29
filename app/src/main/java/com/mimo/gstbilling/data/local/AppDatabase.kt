package com.mimo.gstbilling.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*

@Database(
    entities = [
        CompanyEntity::class,
        PartyEntity::class,
        ItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun partyDao(): PartyDao
    abstract fun itemDao(): ItemDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
}

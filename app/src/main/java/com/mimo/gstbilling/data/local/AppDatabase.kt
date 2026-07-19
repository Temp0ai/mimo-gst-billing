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
        TransactionEntity::class,
        ExpenseEntity::class,
        StoreEntity::class,
        ManufacturingEntity::class,
        StockTransferEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        PartyGroupEntity::class,
        ItemBatchEntity::class,
        ItemVariantEntity::class,
        StaffEntity::class,
        WarehouseEntity::class,
        BillOfMaterialsEntity::class,
        BomItemEntity::class,
        RecurringInvoiceEntity::class,
        DiscountConfigEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun partyDao(): PartyDao
    abstract fun itemDao(): ItemDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun transactionDao(): TransactionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun storeDao(): StoreDao
    abstract fun manufacturingDao(): ManufacturingDao
    abstract fun stockTransferDao(): StockTransferDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun partyGroupDao(): PartyGroupDao
    abstract fun itemBatchDao(): ItemBatchDao
    abstract fun itemVariantDao(): ItemVariantDao
    abstract fun staffDao(): StaffDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun bomDao(): BomDao
    abstract fun recurringInvoiceDao(): RecurringInvoiceDao
    abstract fun discountConfigDao(): DiscountConfigDao
}

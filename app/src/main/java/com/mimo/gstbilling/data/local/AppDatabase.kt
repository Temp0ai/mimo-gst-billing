package com.mimo.gstbilling.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        DiscountConfigEntity::class,
        ChequeEntity::class,
        LoanEntity::class,
        LoanTransactionEntity::class,
        AssetEntity::class,
        AssetDepreciationEntity::class,
        CashAdjustmentEntity::class,
        TransferEntity::class,
        TcsRateEntity::class,
        TcsTransactionEntity::class,
        AuditLogEntity::class,
        KycEntity::class,
        UnitMappingEntity::class,
        OnlineOrderEntity::class
    ],
    version = 16,
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
    abstract fun chequeDao(): ChequeDao
    abstract fun loanDao(): LoanDao
    abstract fun loanTransactionDao(): LoanTransactionDao
    abstract fun assetDao(): AssetDao
    abstract fun assetDepreciationDao(): AssetDepreciationDao
    abstract fun cashAdjustmentDao(): CashAdjustmentDao
    abstract fun transferDao(): TransferDao
    abstract fun tcsRateDao(): TcsRateDao
    abstract fun tcsTransactionDao(): TcsTransactionDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun kycDao(): KycDao
    abstract fun unitMappingDao(): UnitMappingDao
    abstract fun onlineOrderDao(): OnlineOrderDao

    companion object {
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `items` ADD COLUMN `isActive` INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cheques` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `partyId` INTEGER NOT NULL,
                        `chequeNumber` TEXT NOT NULL,
                        `bankName` TEXT NOT NULL,
                        `chequeDate` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `type` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `loans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `partyId` INTEGER NOT NULL,
                        `loanName` TEXT NOT NULL,
                        `loanType` TEXT NOT NULL,
                        `principalAmount` REAL NOT NULL,
                        `interestRate` REAL NOT NULL,
                        `interestType` TEXT NOT NULL,
                        `tenure` INTEGER NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `emiAmount` REAL NOT NULL,
                        `outstandingAmount` REAL NOT NULL,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `loan_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `loanId` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `type` TEXT NOT NULL,
                        `paymentDate` INTEGER NOT NULL,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`loanId`) REFERENCES `loans`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fixed_assets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `assetName` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `purchaseDate` INTEGER NOT NULL,
                        `purchasePrice` REAL NOT NULL,
                        `salvageValue` REAL NOT NULL,
                        `usefulLife` INTEGER NOT NULL,
                        `depreciationMethod` TEXT NOT NULL,
                        `currentValue` REAL NOT NULL,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `asset_depreciations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `assetId` INTEGER NOT NULL,
                        `period` TEXT NOT NULL,
                        `depreciationAmount` REAL NOT NULL,
                        `accumulatedDepreciation` REAL NOT NULL,
                        `bookValue` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`assetId`) REFERENCES `fixed_assets`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cash_adjustments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `type` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `adjustmentDate` INTEGER NOT NULL,
                        `reason` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transfers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `fromAccount` TEXT NOT NULL,
                        `toAccount` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `transferDate` INTEGER NOT NULL,
                        `chequeNumber` TEXT,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tcs_rates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `section` TEXT NOT NULL,
                        `rate` REAL NOT NULL,
                        `minAmount` REAL NOT NULL,
                        `description` TEXT,
                        `isActive` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tcs_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `invoiceId` INTEGER NOT NULL,
                        `partyId` INTEGER NOT NULL,
                        `tcsRateId` INTEGER NOT NULL,
                        `taxableAmount` REAL NOT NULL,
                        `tcsAmount` REAL NOT NULL,
                        `depositionStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `audit_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `userId` TEXT NOT NULL,
                        `action` TEXT NOT NULL,
                        `entity` TEXT NOT NULL,
                        `entityId` INTEGER NOT NULL,
                        `details` TEXT,
                        `timestamp` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `kyc_documents` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `partyId` INTEGER NOT NULL,
                        `documentType` TEXT NOT NULL,
                        `documentNumber` TEXT NOT NULL,
                        `documentPath` TEXT,
                        `verificationStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `unit_mappings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `fromUnit` TEXT NOT NULL,
                        `toUnit` TEXT NOT NULL,
                        `conversionFactor` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `online_orders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL DEFAULT 1,
                        `orderId` TEXT NOT NULL,
                        `partyId` INTEGER,
                        `customerName` TEXT NOT NULL,
                        `customerPhone` TEXT,
                        `totalAmount` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `channel` TEXT NOT NULL,
                        `shippingAddress` TEXT,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}

package com.mimo.gstbilling.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mimo.gstbilling.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mimo_gst_billing_db"
        ).fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    try {
                        val inputStream = context.resources.openRawResource(
                            context.resources.getIdentifier("prepopulate", "raw", context.packageName)
                        )
                        val sql = inputStream.bufferedReader().use { it.readText() }
                        inputStream.close()
                        sql.split("\n").filter { it.isNotBlank() }.forEach { line ->
                            try { db.execSQL(line) } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {}
                }
            })
            .build()
    }

    @Provides fun provideCompanyDao(db: AppDatabase) = db.companyDao()
    @Provides fun providePartyDao(db: AppDatabase) = db.partyDao()
    @Provides fun provideItemDao(db: AppDatabase) = db.itemDao()
    @Provides fun provideInvoiceDao(db: AppDatabase) = db.invoiceDao()
    @Provides fun provideInvoiceItemDao(db: AppDatabase) = db.invoiceItemDao()
    @Provides fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun provideExpenseDao(db: AppDatabase) = db.expenseDao()
    @Provides fun provideStoreDao(db: AppDatabase) = db.storeDao()
    @Provides fun provideManufacturingDao(db: AppDatabase) = db.manufacturingDao()
    @Provides fun provideStockTransferDao(db: AppDatabase) = db.stockTransferDao()
    @Provides fun provideOrderDao(db: AppDatabase) = db.orderDao()
    @Provides fun providePartyGroupDao(db: AppDatabase) = db.partyGroupDao()
    @Provides fun provideItemBatchDao(db: AppDatabase) = db.itemBatchDao()
}

package com.mimo.gstbilling.di

import android.content.Context
import androidx.room.Room
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
            "mimo_gst_database"
        ).build()
    }

    @Provides
    fun provideCompanyDao(database: AppDatabase) = database.companyDao()

    @Provides
    fun providePartyDao(database: AppDatabase) = database.partyDao()

    @Provides
    fun provideItemDao(database: AppDatabase) = database.itemDao()

    @Provides
    fun provideInvoiceDao(database: AppDatabase) = database.invoiceDao()

    @Provides
    fun provideInvoiceItemDao(database: AppDatabase) = database.invoiceItemDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase) = database.transactionDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase) = database.expenseDao()
}

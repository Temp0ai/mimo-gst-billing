package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.DiscountConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscountConfigDao {
    @Query("SELECT * FROM discount_configs WHERE companyId = :companyId ORDER BY name ASC")
    fun getDiscountsByCompany(companyId: Long): Flow<List<DiscountConfigEntity>>

    @Query("SELECT * FROM discount_configs WHERE companyId = :companyId AND type = :type AND isActive = 1")
    fun getDiscountsByType(companyId: Long, type: String): Flow<List<DiscountConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscount(discount: DiscountConfigEntity): Long

    @Update
    suspend fun updateDiscount(discount: DiscountConfigEntity)

    @Delete
    suspend fun deleteDiscount(discount: DiscountConfigEntity)
}

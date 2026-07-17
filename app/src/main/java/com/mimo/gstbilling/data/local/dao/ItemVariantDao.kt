package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.ItemVariantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemVariantDao {
    @Query("SELECT * FROM item_variants WHERE itemId = :itemId ORDER BY variantName ASC")
    fun getVariantsByItem(itemId: Long): Flow<List<ItemVariantEntity>>

    @Query("SELECT * FROM item_variants ORDER BY variantName ASC")
    fun getAllVariants(): Flow<List<ItemVariantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ItemVariantEntity)

    @Update
    suspend fun updateVariant(variant: ItemVariantEntity)

    @Delete
    suspend fun deleteVariant(variant: ItemVariantEntity)

    @Query("SELECT * FROM item_variants WHERE id = :id")
    suspend fun getVariantById(id: Long): ItemVariantEntity?
}

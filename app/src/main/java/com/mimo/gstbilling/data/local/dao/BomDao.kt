package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.BillOfMaterialsEntity
import com.mimo.gstbilling.data.local.entity.BomItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BomDao {
    @Query("SELECT * FROM bill_of_materials WHERE companyId = :companyId ORDER BY name ASC")
    fun getBomsByCompany(companyId: Long): Flow<List<BillOfMaterialsEntity>>

    @Query("SELECT * FROM bill_of_materials WHERE id = :id")
    suspend fun getBomById(id: Long): BillOfMaterialsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBom(bom: BillOfMaterialsEntity): Long

    @Update
    suspend fun updateBom(bom: BillOfMaterialsEntity)

    @Delete
    suspend fun deleteBom(bom: BillOfMaterialsEntity)

    @Query("SELECT * FROM bom_items WHERE bomId = :bomId")
    fun getBomItems(bomId: Long): Flow<List<BomItemEntity>>

    @Query("SELECT * FROM bom_items WHERE bomId = :bomId")
    suspend fun getBomItemsDirect(bomId: Long): List<BomItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBomItems(items: List<BomItemEntity>)

    @Delete
    suspend fun deleteBomItem(item: BomItemEntity)

    @Query("DELETE FROM bom_items WHERE bomId = :bomId")
    suspend fun deleteAllBomItems(bomId: Long)
}

package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.WarehouseEntity
import com.mimo.gstbilling.data.local.entity.StockTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses WHERE companyId = :companyId ORDER BY isDefault DESC, name ASC")
    fun getWarehousesByCompany(companyId: Long): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouses WHERE companyId = :companyId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultWarehouse(companyId: Long): WarehouseEntity?

    @Query("SELECT * FROM warehouses WHERE id = :id")
    suspend fun getWarehouseById(id: Long): WarehouseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: WarehouseEntity): Long

    @Update
    suspend fun updateWarehouse(warehouse: WarehouseEntity)

    @Delete
    suspend fun deleteWarehouse(warehouse: WarehouseEntity)

    @Query("SELECT * FROM stock_transfers WHERE companyId = :companyId ORDER BY transferDate DESC")
    fun getStockTransfers(companyId: Long): Flow<List<StockTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransfer(transfer: StockTransferEntity): Long

    @Update
    suspend fun updateStockTransfer(transfer: StockTransferEntity)

    @Delete
    suspend fun deleteStockTransfer(transfer: StockTransferEntity)
}

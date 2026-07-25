package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.OnlineOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OnlineOrderDao {
    @Query("SELECT * FROM online_orders WHERE companyId = :companyId ORDER BY createdAt DESC")
    fun getOrdersByCompany(companyId: Long): Flow<List<OnlineOrderEntity>>

    @Query("SELECT * FROM online_orders WHERE companyId = :companyId AND status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(companyId: Long, status: String): Flow<List<OnlineOrderEntity>>

    @Query("SELECT * FROM online_orders WHERE companyId = :companyId AND channel = :channel ORDER BY createdAt DESC")
    fun getOrdersByChannel(companyId: Long, channel: String): Flow<List<OnlineOrderEntity>>

    @Query("SELECT * FROM online_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OnlineOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OnlineOrderEntity): Long

    @Update
    suspend fun updateOrder(order: OnlineOrderEntity)

    @Delete
    suspend fun deleteOrder(order: OnlineOrderEntity)

    @Query("SELECT SUM(totalAmount) FROM online_orders WHERE companyId = :companyId AND status != 'cancelled'")
    suspend fun getTotalOrderAmount(companyId: Long): Double?

    @Query("SELECT * FROM online_orders WHERE companyId = :companyId AND orderId = :orderId LIMIT 1")
    suspend fun getOrderByOrderId(companyId: Long, orderId: String): OnlineOrderEntity?
}

package com.mimo.gstbilling.data.local.dao

import androidx.room.*
import com.mimo.gstbilling.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE companyId = :companyId ORDER BY orderDate DESC")
    fun getOrdersByCompany(companyId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE companyId = :companyId AND orderType = :type ORDER BY orderDate DESC")
    fun getOrdersByType(companyId: Long, type: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("SELECT COUNT(*) FROM orders WHERE companyId = :companyId AND orderType = :type")
    suspend fun getOrderCount(companyId: Long, type: String): Int

    @Query("SELECT SUM(totalAmount) FROM orders WHERE companyId = :companyId AND orderType = :type AND status = 'pending'")
    suspend fun getPendingOrderTotal(companyId: Long, type: String): Double?
}

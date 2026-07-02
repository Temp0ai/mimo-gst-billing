package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.OrderDao
import com.mimo.gstbilling.data.local.entity.OrderEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderDao: OrderDao
) : ViewModel() {

    private val companyId = 1L

    fun getOrders(type: String): Flow<List<OrderEntity>> = orderDao.getOrdersByType(companyId, type)

    fun addOrder(partyId: Long, orderNumber: String, type: String, totalAmount: Double, discount: Double, taxAmount: Double, notes: String?) {
        viewModelScope.launch {
            orderDao.insertOrder(
                OrderEntity(
                    companyId = companyId, partyId = partyId, orderNumber = orderNumber,
                    orderType = type, orderDate = System.currentTimeMillis(), deliveryDate = null,
                    totalAmount = totalAmount, discount = discount, taxAmount = taxAmount, notes = notes
                )
            )
        }
    }

    fun updateOrderStatus(order: OrderEntity, status: String) {
        viewModelScope.launch { orderDao.updateOrder(order.copy(status = status)) }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch { orderDao.deleteOrder(order) }
    }

    suspend fun getOrderCount(type: String): Int = orderDao.getOrderCount(companyId, type)
}

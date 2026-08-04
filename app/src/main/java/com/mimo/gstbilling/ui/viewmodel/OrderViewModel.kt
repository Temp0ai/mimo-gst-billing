package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.dao.OrderDao
import com.mimo.gstbilling.data.local.dao.OrderItemDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.data.local.dao.InvoiceDao
import com.mimo.gstbilling.data.local.dao.InvoiceItemDao
import com.mimo.gstbilling.data.local.entity.*
import com.mimo.gstbilling.utils.RecycleBinHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val partyDao: PartyDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val companyDao: CompanyDao,
    private val recycleBinHelper: RecycleBinHelper
) : ViewModel() {
    private var _cachedCompanyId: Long = 1L
    private suspend fun getCurrentCompanyId(): Long {
        if (_cachedCompanyId == 1L) _cachedCompanyId = companyDao.getSelectedCompany().first()?.id ?: 1L
        return _cachedCompanyId
    }

    fun getOrders(orderType: String = "sales_order"): Flow<List<OrderEntity>> = flow {
        emitAll(orderDao.getOrdersByType(getCurrentCompanyId(), orderType))
    }

    fun getOrderById(id: Long): Flow<OrderEntity?> = flow {
        emit(orderDao.getOrderById(id))
    }

    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> =
        orderItemDao.getItemsForOrder(orderId)

    fun getPartyById(id: Long): Flow<PartyEntity?> = flow {
        emit(partyDao.getPartyById(id))
    }

    suspend fun getPartyName(partyId: Long): String {
        return partyDao.getPartyById(partyId)?.name ?: "Unknown"
    }

    suspend fun getOrderNumber(orderType: String): String {
        val count = orderDao.getOrderCountByType(getCurrentCompanyId(), orderType)
        val prefix = if (orderType == "sales_order") "SO" else "PO"
        return "$prefix-${String.format("%04d", count + 1)}"
    }

    suspend fun companyId(): Long = getCurrentCompanyId()

    fun createOrder(order: OrderEntity, items: List<OrderItemEntity>): Flow<Long> = flow {
        val orderId = orderDao.insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        orderItemDao.insertAll(itemsWithOrderId)
        emit(orderId)
    }

    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            val order = orderDao.getOrderById(orderId) ?: return@launch
            orderDao.updateOrder(order.copy(status = status))
        }
    }

    fun convertOrderToInvoice(orderId: Long): Flow<Long> = flow {
        val order = orderDao.getOrderById(orderId) ?: throw Exception("Order not found")
        val orderItems = orderItemDao.getItemsForOrderDirect(orderId)
        val companyId = getCurrentCompanyId()

        val invoice = InvoiceEntity(
            companyId = companyId,
            partyId = order.partyId,
            invoiceNumber = "INV-${java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(System.currentTimeMillis())}-${String.format("%04d", System.currentTimeMillis() % 10000)}",
            invoiceDate = System.currentTimeMillis(),
            dueDate = order.dueDate,
            subTotal = order.subTotal,
            discount = order.discount,
            discountType = order.discountType,
            taxableAmount = order.taxableAmount,
            cgstTotal = order.cgstTotal,
            sgstTotal = order.sgstTotal,
            igstTotal = order.igstTotal,
            totalAmount = order.totalAmount,
            notes = order.notes,
            invoiceType = if (order.orderType == "sales_order") "sales" else "purchase"
        )
        val invoiceId = invoiceDao.insertInvoice(invoice)

        val invoiceItems = orderItems.map { oi ->
            InvoiceItemEntity(
                invoiceId = invoiceId,
                itemId = oi.itemId,
                itemName = oi.itemName,
                hsnCode = oi.hsnCode,
                quantity = oi.quantity,
                unit = oi.unit,
                price = oi.price,
                discount = oi.discount,
                gstRate = oi.gstRate,
                taxableAmount = oi.taxableAmount,
                cgstAmount = oi.cgstAmount,
                sgstAmount = oi.sgstAmount,
                igstAmount = oi.igstAmount,
                totalAmount = oi.totalAmount
            )
        }
        invoiceItemDao.insertAll(invoiceItems)
        updateOrderStatus(orderId, "completed")
        emit(invoiceId)
    }

    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            val order = orderDao.getOrderById(orderId)
            if (order != null) recycleBinHelper.deleteOrderToBin(order)
        }
    }
}

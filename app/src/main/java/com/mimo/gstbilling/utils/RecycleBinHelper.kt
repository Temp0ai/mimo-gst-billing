package com.mimo.gstbilling.utils

import com.google.gson.Gson
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecycleBinHelper @Inject constructor(
    private val deletedItemDao: DeletedItemDao,
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val partyDao: PartyDao,
    private val itemDao: ItemDao,
    private val expenseDao: ExpenseDao,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao
) {
    private val gson = Gson()

    private suspend fun getCompanyId(): Long {
        return companyDao.getSelectedCompany().first()?.id ?: 1L
    }

    private suspend fun addToBin(entityType: String, entityId: Long, entityName: String, amount: Double, entityData: String) {
        deletedItemDao.insert(
            DeletedItemEntity(
                companyId = getCompanyId(),
                entityType = entityType,
                entityId = entityId,
                entityName = entityName,
                amount = amount,
                entityData = entityData
            )
        )
    }

    suspend fun deleteInvoiceToBin(invoice: InvoiceEntity) {
        val items = invoiceItemDao.getItemsForInvoice(invoice.id)
        val fullData = gson.toJson(mapOf("invoice" to invoice, "items" to items))
        addToBin("Invoice", invoice.id, invoice.invoiceNumber, invoice.totalAmount, fullData)
        invoiceItemDao.deleteItemsForInvoice(invoice.id)
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun deleteItemToBin(item: ItemEntity) {
        val fullData = gson.toJson(item)
        addToBin("Item", item.id, item.name, item.salePrice, fullData)
        itemDao.deleteItem(item)
    }

    suspend fun deletePartyToBin(party: PartyEntity) {
        val fullData = gson.toJson(party)
        addToBin("Party", party.id, party.name, party.balance, fullData)
        partyDao.deleteParty(party)
    }

    suspend fun deleteExpenseToBin(expense: ExpenseEntity) {
        val fullData = gson.toJson(expense)
        addToBin("Expense", expense.id, expense.category, expense.amount, fullData)
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteOrderToBin(order: OrderEntity) {
        val items = orderItemDao.getItemsForOrderDirect(order.id)
        val fullData = gson.toJson(mapOf("order" to order, "items" to items))
        addToBin("Order", order.id, order.orderNumber, order.totalAmount, fullData)
        orderItemDao.deleteItemsForOrder(order.id)
        orderDao.deleteOrder(order)
    }
}

package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val deletedItemDao: DeletedItemDao,
    private val companyDao: CompanyDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val partyDao: PartyDao,
    private val itemDao: ItemDao,
    private val expenseDao: ExpenseDao,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao
) : ViewModel() {

    private val _companyId = MutableStateFlow(1L)
    private val gson = Gson()

    val deletedItems: StateFlow<List<DeletedItemEntity>> = _companyId.flatMapLatest { companyId ->
        deletedItemDao.getDeletedItems(companyId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedCount: StateFlow<Int> = _companyId.flatMapLatest { companyId ->
        deletedItemDao.getDeletedCount(companyId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            companyDao.getSelectedCompany().collect { company ->
                _companyId.value = company?.id ?: 1L
            }
        }
    }

    fun addToRecycleBin(entityType: String, entityId: Long, entityName: String, amount: Double, entityData: String) {
        viewModelScope.launch {
            deletedItemDao.insert(
                DeletedItemEntity(
                    companyId = _companyId.value,
                    entityType = entityType,
                    entityId = entityId,
                    entityName = entityName,
                    amount = amount,
                    entityData = entityData
                )
            )
        }
    }

    fun deleteInvoiceToBin(invoice: InvoiceEntity) {
        viewModelScope.launch {
            val items = invoiceItemDao.getItemsForInvoice(invoice.id)
            val fullData = gson.toJson(mapOf("invoice" to invoice, "items" to items))
            addToRecycleBin("Invoice", invoice.id, invoice.invoiceNumber, invoice.totalAmount, fullData)
            invoiceItemDao.deleteItemsForInvoice(invoice.id)
            invoiceDao.deleteInvoice(invoice)
        }
    }

    fun deleteItemToBin(item: ItemEntity) {
        viewModelScope.launch {
            val fullData = gson.toJson(item)
            addToRecycleBin("Item", item.id, item.name, item.salePrice, fullData)
            itemDao.deleteItem(item)
        }
    }

    fun deletePartyToBin(party: PartyEntity) {
        viewModelScope.launch {
            val fullData = gson.toJson(party)
            addToRecycleBin("Party", party.id, party.name, party.balance, fullData)
            partyDao.deleteParty(party)
        }
    }

    fun deleteExpenseToBin(expense: ExpenseEntity) {
        viewModelScope.launch {
            val fullData = gson.toJson(expense)
            addToRecycleBin("Expense", expense.id, expense.category, expense.amount, fullData)
            expenseDao.deleteExpense(expense)
        }
    }

    fun deleteOrderToBin(order: OrderEntity) {
        viewModelScope.launch {
            val items = orderItemDao.getItemsForOrderDirect(order.id)
            val fullData = gson.toJson(mapOf("order" to order, "items" to items))
            addToRecycleBin("Order", order.id, order.orderNumber, order.totalAmount, fullData)
            orderItemDao.deleteItemsForOrder(order.id)
            orderDao.deleteOrder(order)
        }
    }

    fun restore(deletedItem: DeletedItemEntity) {
        viewModelScope.launch {
            when (deletedItem.entityType) {
                "Invoice" -> {
                    val map = gson.fromJson(deletedItem.entityData, Map::class.java) as? Map<*, *>
                    map?.let {
                        val invoiceJson = gson.toJson(it["invoice"])
                        val invoice = gson.fromJson(invoiceJson, InvoiceEntity::class.java)
                        invoiceDao.insertInvoice(invoice)
                        val itemsJson = gson.toJson(it["items"])
                        val items = gson.fromJson(itemsJson, Array<InvoiceItemEntity>::class.java)?.toList() ?: emptyList()
                        items.forEach { item -> invoiceItemDao.insertInvoiceItem(item) }
                    }
                }
                "Item" -> {
                    val item = gson.fromJson(deletedItem.entityData, ItemEntity::class.java)
                    itemDao.insertItem(item)
                }
                "Party" -> {
                    val party = gson.fromJson(deletedItem.entityData, PartyEntity::class.java)
                    partyDao.insertParty(party)
                }
                "Expense" -> {
                    val expense = gson.fromJson(deletedItem.entityData, ExpenseEntity::class.java)
                    expenseDao.insertExpense(expense)
                }
                "Order" -> {
                    val map = gson.fromJson(deletedItem.entityData, Map::class.java) as? Map<*, *>
                    map?.let {
                        val orderJson = gson.toJson(it["order"])
                        val order = gson.fromJson(orderJson, OrderEntity::class.java)
                        orderDao.insertOrder(order)
                        val itemsJson = gson.toJson(it["items"])
                        val items = gson.fromJson(itemsJson, Array<OrderItemEntity>::class.java)?.toList() ?: emptyList()
                        items.forEach { item -> orderItemDao.insertAll(listOf(item)) }
                    }
                }
            }
            deletedItemDao.deleteById(deletedItem.id)
        }
    }

    fun permanentlyDelete(deletedItem: DeletedItemEntity) {
        viewModelScope.launch {
            deletedItemDao.deleteById(deletedItem.id)
        }
    }

    fun emptyAll() {
        viewModelScope.launch {
            deletedItemDao.deleteAll(_companyId.value)
        }
    }
}

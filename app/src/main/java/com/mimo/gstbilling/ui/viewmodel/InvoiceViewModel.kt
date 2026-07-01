package com.mimo.gstbilling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceUiState(
    val companyId: Long = 1L,
    val partyId: Long = 0L,
    val partyName: String = "",
    val partyPhone: String = "",
    val invoiceNumber: String = "",
    val invoiceDate: Long = System.currentTimeMillis(),
    val items: List<InvoiceItemModel> = emptyList(),
    val discount: Double = 0.0,
    val discountType: String = "percentage",
    val taxableAmount: Double = 0.0,
    val cgstTotal: Double = 0.0,
    val sgstTotal: Double = 0.0,
    val igstTotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedInvoiceId: Long? = null,
    val allItems: List<ItemEntity> = emptyList(),
    val allParties: List<PartyEntity> = emptyList()
)

data class InvoiceItemModel(
    val itemId: Long = 0,
    val itemName: String = "",
    val hsnCode: String = "",
    val quantity: Double = 1.0,
    val unit: String = "Pcs",
    val price: Double = 0.0,
    val discount: Double = 0.0,
    val gstRate: Double = 18.0,
    val taxableAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalAmount: Double = 0.0
)

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val itemDao: ItemDao,
    private val partyDao: PartyDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    private val companyId = 1L

    init {
        loadReferenceData()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            combine(
                itemDao.getItemsByCompany(companyId),
                partyDao.getPartiesByCompany(companyId)
            ) { items, parties ->
                _uiState.value.copy(allItems = items, allParties = parties)
            }.collect { _uiState.value = it }
        }
        viewModelScope.launch {
            val count = invoiceDao.getSalesInvoiceCount(companyId)
            _uiState.update { it.copy(invoiceNumber = "INV-${String.format("%04d", count + 1)}") }
        }
    }

    fun setParty(partyId: Long, name: String, phone: String) {
        _uiState.update { it.copy(partyId = partyId, partyName = name, partyPhone = phone) }
    }

    fun addItem(item: ItemEntity) {
        val qty = 1.0
        val taxable = item.salePrice * qty
        val cgst = taxable * item.gstRate / 200
        val sgst = taxable * item.gstRate / 200
        val total = taxable + cgst + sgst
        val model = InvoiceItemModel(
            itemId = item.id,
            itemName = item.name,
            hsnCode = item.hsnCode ?: "",
            quantity = qty,
            unit = item.unit,
            price = item.salePrice,
            gstRate = item.gstRate,
            taxableAmount = taxable,
            cgstAmount = cgst,
            sgstAmount = sgst,
            igstAmount = 0.0,
            totalAmount = total
        )
        _uiState.update { state ->
            val updated = state.items + model
            state.copy(items = updated).recalculate()
        }
    }

    fun updateItemQuantity(index: Int, qty: Double) {
        _uiState.update { state ->
            if (index < 0 || index >= state.items.size) return@update state
            val item = state.items[index]
            val taxable = item.price * qty
            val cgst = taxable * item.gstRate / 200
            val sgst = taxable * item.gstRate / 200
            val total = taxable + cgst + sgst
            val updated = item.copy(
                quantity = qty,
                taxableAmount = taxable,
                cgstAmount = cgst,
                sgstAmount = sgst,
                totalAmount = total
            )
            val items = state.items.toMutableList()
            items[index] = updated
            state.copy(items = items).recalculate()
        }
    }

    fun removeItem(index: Int) {
        _uiState.update { state ->
            if (index < 0 || index >= state.items.size) return@update state
            val items = state.items.toMutableList()
            items.removeAt(index)
            state.copy(items = items).recalculate()
        }
    }

    fun updateDiscount(value: String) {
        val discount = value.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(discount = discount).recalculate() }
    }

    fun setDiscountType(type: String) {
        _uiState.update { it.copy(discountType = type).recalculate() }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    private fun InvoiceUiState.recalculate(): InvoiceUiState {
        val itemsTotal = items.sumOf { it.totalAmount }
        val discountAmount = if (discountType == "percentage") itemsTotal * discount / 100 else discount
        val totalTaxable = items.sumOf { it.taxableAmount }
        val totalCgst = items.sumOf { it.cgstAmount }
        val totalSgst = items.sumOf { it.sgstAmount }
        val totalIgst = items.sumOf { it.igstAmount }
        val finalTotal = totalTaxable + totalCgst + totalSgst + totalIgst - discountAmount
        return copy(
            taxableAmount = totalTaxable,
            cgstTotal = totalCgst,
            sgstTotal = totalSgst,
            igstTotal = totalIgst,
            totalAmount = finalTotal
        )
    }

    fun saveInvoice() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val invoice = InvoiceEntity(
                companyId = companyId,
                partyId = state.partyId,
                invoiceNumber = state.invoiceNumber,
                invoiceDate = state.invoiceDate,
                dueDate = null,
                subTotal = state.items.sumOf { it.totalAmount },
                discount = state.discount,
                discountType = state.discountType,
                taxableAmount = state.taxableAmount,
                cgstTotal = state.cgstTotal,
                sgstTotal = state.sgstTotal,
                igstTotal = state.igstTotal,
                totalAmount = state.totalAmount,
                notes = state.notes.ifBlank { null }
            )
            val invoiceId = invoiceDao.insertInvoice(invoice)
            val invoiceItems = state.items.map { item ->
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    itemId = item.itemId,
                    itemName = item.itemName,
                    hsnCode = item.hsnCode.ifBlank { null },
                    quantity = item.quantity,
                    unit = item.unit,
                    price = item.price,
                    discount = item.discount,
                    gstRate = item.gstRate,
                    taxableAmount = item.taxableAmount,
                    cgstAmount = item.cgstAmount,
                    sgstAmount = item.sgstAmount,
                    igstAmount = item.igstAmount,
                    totalAmount = item.totalAmount
                )
            }
            invoiceItemDao.insertAll(invoiceItems)
            _uiState.update { it.copy(isSaving = false, savedInvoiceId = invoiceId) }
        }
    }

    fun resetState() {
        viewModelScope.launch {
            val count = invoiceDao.getSalesInvoiceCount(companyId)
            _uiState.value = InvoiceUiState(
                invoiceNumber = "INV-${String.format("%04d", count + 1)}"
            )
            loadReferenceData()
        }
    }

    fun getInvoices(type: String): Flow<List<InvoiceEntity>> {
        return invoiceDao.getInvoicesByType(companyId, type)
    }

    fun getInvoiceById(id: Long): Flow<InvoiceEntity?> {
        return flow { emit(invoiceDao.getInvoiceById(id)) }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch { invoiceDao.deleteInvoice(invoice) }
    }

    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity> {
        return invoiceItemDao.getItemsForInvoice(invoiceId)
    }
}

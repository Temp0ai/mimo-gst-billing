package com.mimo.gstbilling.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel
import com.mimo.gstbilling.data.local.entity.ItemEntity
import com.mimo.gstbilling.data.local.entity.ItemVariantEntity

data class DetailVariantFormState(
    val variantName: String = "",
    val salePrice: String = "",
    val purchasePrice: String = "",
    val stockQuantity: String = "0",
    val unit: String = "NOS",
    val sku: String = "",
    val barcode: String = "",
    val editingId: Long? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    navController: NavController,
    itemId: Long,
    viewModel: ItemViewModel = hiltViewModel()
) {
    var item by remember { mutableStateOf<ItemEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val variants by viewModel.getVariantsByItem(itemId).collectAsState(initial = emptyList())
    var showVariantForm by remember { mutableStateOf(false) }
    var variantForm by remember { mutableStateOf(DetailVariantFormState()) }
    var showVariantUnitMenu by remember { mutableStateOf(false) }
    var showDeleteVariantDialog by remember { mutableStateOf<ItemVariantEntity?>(null) }
    val unitOptions = listOf("NOS", "PCS", "KG", "GM", "M", "FT", "L", "ML", "BOX", "SET", "PAIR")

    LaunchedEffect(itemId) {
        item = viewModel.getItemById(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState())) {
            item?.imageUri?.let { uri ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(uri)),
                        contentDescription = "Item Image",
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(item?.name ?: "Item", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(if (item?.isService == true) "Service" else "Product", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    DetailRow("HSN Code", item?.hsnCode ?: "N/A")
                    DetailRow("Sale Price", String.format(java.util.Locale.US, "\u20B9%,.2f", item?.salePrice ?: 0.0))
                    DetailRow("Purchase Price", String.format(java.util.Locale.US, "\u20B9%,.2f", item?.purchasePrice ?: 0.0))
                    DetailRow("GST Rate", "${item?.gstRate?.toInt() ?: 0}%")
                    DetailRow("Unit", item?.unit ?: "NOS")
                    DetailRow("Stock", "${item?.stockQuantity?.toInt() ?: 0}")
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Variants", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        TextButton(onClick = {
                            variantForm = DetailVariantFormState(unit = item?.unit ?: "NOS")
                            showVariantForm = true
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Variant", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (variants.isEmpty() && !showVariantForm) {
                        Text("No variants added yet", fontSize = 13.sp, color = TextSecondary)
                    }

                    variants.forEach { variant ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(variant.variantName, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                                    Text("\u20B9${variant.salePrice} | Stock: ${variant.stockQuantity.toInt()}", fontSize = 12.sp, color = TextSecondary)
                                    if (!variant.sku.isNullOrBlank()) {
                                        Text("SKU: ${variant.sku}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                                Row {
                                    IconButton(onClick = {
                                        variantForm = DetailVariantFormState(
                                            variantName = variant.variantName,
                                            salePrice = variant.salePrice.toString(),
                                            purchasePrice = variant.purchasePrice.toString(),
                                            stockQuantity = variant.stockQuantity.toString(),
                                            unit = variant.unit,
                                            sku = variant.sku ?: "",
                                            barcode = variant.barcode ?: "",
                                            editingId = variant.id
                                        )
                                        showVariantForm = true
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Primary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { showDeleteVariantDialog = variant }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (showVariantForm) {
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Text(
                            if (variantForm.editingId != null) "Edit Variant" else "Add Variant",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                        )
                        OutlinedTextField(
                            value = variantForm.variantName,
                            onValueChange = { variantForm = variantForm.copy(variantName = it) },
                            label = { Text("Variant Name *", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            placeholder = { Text("e.g. Red - XL", fontSize = 13.sp) }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = variantForm.salePrice,
                                onValueChange = { variantForm = variantForm.copy(salePrice = it) },
                                label = { Text("Sale Price *", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                leadingIcon = { Text("\u20B9", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                            )
                            OutlinedTextField(
                                value = variantForm.purchasePrice,
                                onValueChange = { variantForm = variantForm.copy(purchasePrice = it) },
                                label = { Text("Purchase Price", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                leadingIcon = { Text("\u20B9", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                            )
                        }
                        OutlinedTextField(
                            value = variantForm.stockQuantity,
                            onValueChange = { variantForm = variantForm.copy(stockQuantity = it) },
                            label = { Text("Stock Quantity", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        ExposedDropdownMenuBox(expanded = showVariantUnitMenu, onExpandedChange = { showVariantUnitMenu = it }) {
                            OutlinedTextField(
                                value = variantForm.unit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit", fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showVariantUnitMenu) }
                            )
                            ExposedDropdownMenu(expanded = showVariantUnitMenu, onDismissRequest = { showVariantUnitMenu = false }) {
                                unitOptions.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { variantForm = variantForm.copy(unit = u); showVariantUnitMenu = false }) }
                            }
                        }
                        OutlinedTextField(
                            value = variantForm.sku,
                            onValueChange = { variantForm = variantForm.copy(sku = it) },
                            label = { Text("SKU", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = variantForm.barcode,
                            onValueChange = { variantForm = variantForm.copy(barcode = it) },
                            label = { Text("Barcode", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showVariantForm = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("Cancel", fontWeight = FontWeight.Bold) }
                            Button(
                                onClick = {
                                    if (variantForm.variantName.isNotBlank() && variantForm.salePrice.isNotBlank()) {
                                        if (variantForm.editingId != null) {
                                            viewModel.updateVariant(
                                                ItemVariantEntity(
                                                    id = variantForm.editingId ?: 0L,
                                                    itemId = itemId,
                                                    variantName = variantForm.variantName,
                                                    salePrice = variantForm.salePrice.toDoubleOrNull() ?: 0.0,
                                                    purchasePrice = variantForm.purchasePrice.toDoubleOrNull() ?: 0.0,
                                                    stockQuantity = variantForm.stockQuantity.toDoubleOrNull() ?: 0.0,
                                                    unit = variantForm.unit,
                                                    sku = variantForm.sku.ifBlank { null },
                                                    barcode = variantForm.barcode.ifBlank { null }
                                                )
                                            )
                                        } else {
                                            viewModel.addVariant(
                                                itemId = itemId,
                                                variantName = variantForm.variantName,
                                                salePrice = variantForm.salePrice.toDoubleOrNull() ?: 0.0,
                                                purchasePrice = variantForm.purchasePrice.toDoubleOrNull() ?: 0.0,
                                                stockQuantity = variantForm.stockQuantity.toDoubleOrNull() ?: 0.0,
                                                unit = variantForm.unit,
                                                sku = variantForm.sku,
                                                barcode = variantForm.barcode
                                            )
                                        }
                                        showVariantForm = false
                                        variantForm = DetailVariantFormState(unit = item?.unit ?: "NOS")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("Save Variant", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${item?.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    item?.let { viewModel.deleteItem(it) }
                    showDeleteDialog = false
                    navController.popBackStack()
                }) { Text("Delete", color = RedAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = Primary) }
            }
        )
    }

    if (showDeleteVariantDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteVariantDialog = null },
            title = { Text("Delete Variant", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${showDeleteVariantDialog?.variantName}?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteVariantDialog?.let { viewModel.deleteVariant(it) }
                    showDeleteVariantDialog = null
                }) { Text("Delete", color = RedAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteVariantDialog = null }) { Text("Cancel", color = Primary) }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

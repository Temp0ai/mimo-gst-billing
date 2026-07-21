package com.mimo.gstbilling.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel
import com.mimo.gstbilling.data.local.entity.ItemVariantEntity
import java.io.File

data class VariantFormState(
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
fun AddItemScreen(navController: NavController, viewModel: ItemViewModel = hiltViewModel()) {
    var itemName by remember { mutableStateOf("") }
    var hsnCode by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var gstRate by remember { mutableStateOf("18") }
    var stockQty by remember { mutableStateOf("0") }
    var unit by remember { mutableStateOf("NOS") }
    var isService by remember { mutableStateOf(false) }
    var showGstMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    val gstOptions = listOf("0", "5", "12", "18", "28")
    val unitOptions = listOf("NOS", "PCS", "KG", "GM", "M", "FT", "L", "ML", "BOX", "SET", "PAIR")

    var variants by remember { mutableStateOf<List<VariantFormState>>(emptyList()) }
    var showVariantForm by remember { mutableStateOf(false) }
    var variantForm by remember { mutableStateOf(VariantFormState()) }
    var showVariantUnitMenu by remember { mutableStateOf(false) }
    var showDeleteVariantDialog by remember { mutableStateOf<VariantFormState?>(null) }
    var savedItemId by remember { mutableStateOf<Long?>(null) }

    var itemImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }
    var itemImageFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && itemImageFile != null) {
            itemImageUri = Uri.fromFile(itemImageFile)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            itemImageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add New Item", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary), border = ButtonDefaults.outlinedButtonBorder) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(onClick = {
                    if (itemName.isNotBlank() && salePrice.isNotBlank()) {
                        if (variants.isNotEmpty()) {
                            viewModel.addItemWithVariants(
                                name = itemName,
                                hsnCode = hsnCode.ifBlank { null },
                                description = null,
                                salePrice = salePrice.toDouble(),
                                purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                gstRate = gstRate.toDouble(),
                                unit = unit,
                                stockQuantity = stockQty.toDoubleOrNull() ?: 0.0,
                                isService = isService,
                                variants = variants.map { Triple(it.variantName, it.salePrice.toDouble(), it.stockQuantity.toDoubleOrNull() ?: 0.0) },
                                imageUri = itemImageUri?.toString()
                            )
                        } else {
                            viewModel.addItem(itemName, hsnCode.ifBlank { null }, null, salePrice.toDouble(), purchasePrice.toDoubleOrNull() ?: 0.0, gstRate.toDouble(), unit, stockQty.toDoubleOrNull() ?: 0.0, isService, imageUri = itemImageUri?.toString())
                        }
                        navController.popBackStack()
                    }
                }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState())) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).clickable { showImageOptions = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (itemImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = itemImageUri),
                            contentDescription = "Item Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Change Image",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                                .background(Primary, CircleShape).padding(6.dp).size(18.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Item Image", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name *", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Inventory, contentDescription = null, tint = Primary) })
                    OutlinedTextField(value = hsnCode, onValueChange = { hsnCode = it }, label = { Text("HSN Code", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null, tint = Primary) })

                    ExposedDropdownMenuBox(expanded = showGstMenu, onExpandedChange = { showGstMenu = it }) {
                        OutlinedTextField(value = "$gstRate%", onValueChange = {}, readOnly = true, label = { Text("GST Rate", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(10.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGstMenu) }, leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Primary) })
                        ExposedDropdownMenu(expanded = showGstMenu, onDismissRequest = { showGstMenu = false }) {
                            gstOptions.forEach { rate -> DropdownMenuItem(text = { Text("$rate%") }, onClick = { gstRate = rate; showGstMenu = false }) }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Sale Price *", fontSize = 14.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Text("\u20B9", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
                        OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Purchase Price", fontSize = 14.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Text("\u20B9", color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
                    }

                    OutlinedTextField(value = stockQty, onValueChange = { stockQty = it }, label = { Text("Opening Stock", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Store, contentDescription = null, tint = Primary) })

                    ExposedDropdownMenuBox(expanded = showUnitMenu, onExpandedChange = { showUnitMenu = it }) {
                        OutlinedTextField(value = unit, onValueChange = {}, readOnly = true, label = { Text("Unit", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(10.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) }, leadingIcon = { Icon(Icons.Filled.Straighten, contentDescription = null, tint = Primary) })
                        ExposedDropdownMenu(expanded = showUnitMenu, onDismissRequest = { showUnitMenu = false }) {
                            unitOptions.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; showUnitMenu = false }) }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Service Item", fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = isService, onCheckedChange = { isService = it }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }
                }
            }

            if (!isService) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Variants", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            TextButton(onClick = {
                                variantForm = VariantFormState()
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

                        variants.forEachIndexed { index, variant ->
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
                                        Text("\u20B9${variant.salePrice} | Stock: ${variant.stockQuantity}", fontSize = 12.sp, color = TextSecondary)
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            variantForm = variant
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
                                                val idx = variants.indexOfFirst { it.editingId == variantForm.editingId }
                                                if (idx >= 0) {
                                                    variants = variants.toMutableList().apply {
                                                        set(idx, variantForm)
                                                    }
                                                }
                                            } else {
                                                variants = variants + variantForm
                                            }
                                            showVariantForm = false
                                            variantForm = VariantFormState()
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
    }

    if (showDeleteVariantDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteVariantDialog = null },
            title = { Text("Delete Variant", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${showDeleteVariantDialog?.variantName}?") },
            confirmButton = {
                TextButton(onClick = {
                    variants = variants.filter { it != showDeleteVariantDialog }
                    showDeleteVariantDialog = null
                }) { Text("Delete", color = RedAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteVariantDialog = null }) { Text("Cancel", color = Primary) }
            }
        )
    }

    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Add Image", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable {
                        showImageOptions = false
                        val imageDir = File(context.cacheDir, "item_images")
                        imageDir.mkdirs()
                        val imageFile = File(imageDir, "item_${System.currentTimeMillis()}.jpg")
                        itemImageFile = imageFile
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
                        cameraLauncher.launch(uri)
                    }.padding(12.dp)) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Take Photo", fontSize = 16.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth().clickable {
                        showImageOptions = false
                        galleryLauncher.launch("image/*")
                    }.padding(12.dp)) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Choose from Gallery", fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showImageOptions = false }) { Text("Cancel") } }
        )
    }
}

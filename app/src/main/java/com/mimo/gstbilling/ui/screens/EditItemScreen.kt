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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.ItemViewModel
import com.mimo.gstbilling.utils.HsnDatabase
import java.io.File
import android.net.Uri.fromFile
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(navController: NavController, itemId: Long, viewModel: ItemViewModel = hiltViewModel()) {
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
    var showHsnSuggestions by remember { mutableStateOf(false) }
    val hsnSuggestions = remember(itemName) {
        if (itemName.length >= 2) HsnDatabase.suggestForItem(itemName) else emptyList()
    }
    val gstOptions = listOf("0", "5", "12", "18", "28")
    val unitOptions = listOf("NOS", "PCS", "KG", "GM", "M", "FT", "L", "ML", "BOX", "SET", "PAIR")

    var itemImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }
    var itemImageFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current
    var loaded by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && itemImageFile != null) { itemImageUri = fromFile(itemImageFile) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { itemImageUri = uri }
    }

    LaunchedEffect(itemId) {
        val item = viewModel.getItemById(itemId)
        item?.let {
            itemName = it.name
            hsnCode = it.hsnCode ?: ""
            salePrice = it.salePrice.toString()
            purchasePrice = if (it.purchasePrice > 0) it.purchasePrice.toString() else ""
            gstRate = it.gstRate.toInt().toString()
            stockQty = it.stockQuantity.toInt().toString()
            unit = it.unit
            isService = it.isService
            if (!it.imageUri.isNullOrBlank()) itemImageUri = Uri.parse(it.imageUri)
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Item", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = VyaparTextPrimary, navigationIconContentColor = VyaparTextPrimary))
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(50), colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparBlue), border = ButtonDefaults.outlinedButtonBorder) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(onClick = {
                    if (itemName.isNotBlank() && salePrice.isNotBlank()) {
                         viewModel.updateItem(
                             com.mimo.gstbilling.data.local.entity.ItemEntity(
                                 id = itemId,
                                 companyId = viewModel.getCompanyId(),
                                 name = itemName,
                                hsnCode = hsnCode.ifBlank { null },
                                description = null,
                                salePrice = salePrice.toDouble(),
                                purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                gstRate = gstRate.toDouble(),
                                unit = unit,
                                stockQuantity = stockQty.toDoubleOrNull() ?: 0.0,
                                isService = isService,
                                imageUri = itemImageUri?.toString()
                            )
                        )
                        navController.popBackStack()
                    }
                }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = VyaparRed), enabled = loaded) {
                    Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(VyaparBackground).verticalScroll(rememberScrollState())) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VyaparInputBackground)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).clickable { showImageOptions = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (itemImageUri != null) {
                        Image(painter = rememberAsyncImagePainter(model = itemImageUri), contentDescription = "Item Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Icon(Icons.Filled.Edit, contentDescription = "Change Image", tint = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).background(VyaparBlue, CircleShape).padding(6.dp).size(18.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = VyaparTextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Item Image", fontSize = 12.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = VyaparWhite)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name *", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Inventory, contentDescription = null, tint = VyaparBlue) })

                    Box {
                        OutlinedTextField(value = hsnCode, onValueChange = { hsnCode = it }, label = { Text("HSN Code", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null, tint = VyaparBlue) },
                            trailingIcon = { if (hsnSuggestions.isNotEmpty() && hsnCode.isEmpty()) { IconButton(onClick = { showHsnSuggestions = !showHsnSuggestions }) { Icon(Icons.Filled.Lightbulb, contentDescription = "Suggestions", tint = VyaparBlue) } } })
                        if (hsnSuggestions.isNotEmpty() && (showHsnSuggestions || hsnCode.isEmpty()) && itemName.length >= 2) {
                            Card(modifier = Modifier.fillMaxWidth().padding(top = 56.dp), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("HSN Suggestions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VyaparBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    hsnSuggestions.take(5).forEach { entry ->
                                        Row(modifier = Modifier.fillMaxWidth().clickable { hsnCode = entry.code; gstRate = entry.taxRate.toInt().toString(); showHsnSuggestions = false }.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) { Text(entry.code, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary); Text(entry.description.take(40), fontSize = 11.sp, color = VyaparTextSecondary, maxLines = 1) }
                                            Column(horizontalAlignment = Alignment.End) { Text("${entry.taxRate.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VyaparBlue); Text(entry.category, fontSize = 10.sp, color = VyaparTextSecondary) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ExposedDropdownMenuBox(expanded = showGstMenu, onExpandedChange = { showGstMenu = it }) {
                        OutlinedTextField(value = "$gstRate%", onValueChange = {}, readOnly = true, label = { Text("GST Rate", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(10.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGstMenu) }, leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = VyaparBlue) })
                        ExposedDropdownMenu(expanded = showGstMenu, onDismissRequest = { showGstMenu = false }) {
                            gstOptions.forEach { rate -> DropdownMenuItem(text = { Text("$rate%") }, onClick = { gstRate = rate; showGstMenu = false }) }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Sale Price *", fontSize = 14.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Text("\u20B9", color = VyaparBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
                        OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Purchase Price", fontSize = 14.sp) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Text("\u20B9", color = VyaparBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
                    }

                    OutlinedTextField(value = stockQty, onValueChange = { stockQty = it }, label = { Text("Opening Stock", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Store, contentDescription = null, tint = VyaparBlue) })

                    ExposedDropdownMenuBox(expanded = showUnitMenu, onExpandedChange = { showUnitMenu = it }) {
                        OutlinedTextField(value = unit, onValueChange = {}, readOnly = true, label = { Text("Unit", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(10.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) }, leadingIcon = { Icon(Icons.Filled.Straighten, contentDescription = null, tint = VyaparBlue) })
                        ExposedDropdownMenu(expanded = showUnitMenu, onDismissRequest = { showUnitMenu = false }) {
                            unitOptions.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; showUnitMenu = false }) }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Service Item", fontSize = 15.sp, color = VyaparTextPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = isService, onCheckedChange = { isService = it }, colors = SwitchDefaults.colors(checkedTrackColor = VyaparBlue))
                    }
                }
            }
        }
    }

    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Add Image", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { showImageOptions = false; val imageDir = File(context.cacheDir, "item_images"); imageDir.mkdirs(); val imageFile = File(imageDir, "item_${System.currentTimeMillis()}.jpg"); itemImageFile = imageFile; val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile); cameraLauncher.launch(uri) }.padding(12.dp)) { Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = VyaparBlue); Spacer(modifier = Modifier.width(12.dp)); Text("Take Photo", fontSize = 16.sp) }
                    Row(modifier = Modifier.fillMaxWidth().clickable { showImageOptions = false; galleryLauncher.launch("image/*") }.padding(12.dp)) { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = VyaparBlue); Spacer(modifier = Modifier.width(12.dp)); Text("Choose from Gallery", fontSize = 16.sp) }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showImageOptions = false }) { Text("Cancel") } }
        )
    }
}

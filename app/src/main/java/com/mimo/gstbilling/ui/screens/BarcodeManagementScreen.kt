package com.mimo.gstbilling.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.mimo.gstbilling.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

data class GeneratedBarcode(val code: String, val format: String, val itemName: String, val bitmap: Bitmap)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("CODE128") }
    var barcodeValue by remember { mutableStateOf("") }
    var formatExpanded by remember { mutableStateOf(false) }
    val formats = listOf("CODE128", "CODE39", "EAN13", "EAN8", "UPC_A", "QR_CODE")

    var generatedBarcodes by remember { mutableStateOf<List<GeneratedBarcode>>(emptyList()) }
    var showPreview by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewCode by remember { mutableStateOf("") }

    fun generateBarcodeBitmap(data: String, format: BarcodeFormat, width: Int = 400, height: Int = 200): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(data, format, width, height, hints)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun generateUniqueBarcode(): String {
        return when (selectedFormat) {
            "EAN13" -> {
                val base = "590123412345"
                val checkDigit = calculateEan13CheckDigit(base)
                base + checkDigit
            }
            "EAN8" -> {
                val base = "9638507"
                val checkDigit = calculateEan8CheckDigit(base)
                base + checkDigit
            }
            "UPC_A" -> {
                val base = "01234567890"
                val checkDigit = calculateUpcCheckDigit(base)
                base + checkDigit
            }
            else -> {
                val timestamp = System.currentTimeMillis()
                val random = (1000..9999).random()
                "ITEM${timestamp}$random"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Barcode Management", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Item Details", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Inventory, contentDescription = null, tint = VyaparBlue) })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = itemPrice, onValueChange = { itemPrice = it }, label = { Text("Price (optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Text("\u20B9", color = VyaparBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = barcodeValue, onValueChange = { barcodeValue = it }, label = { Text("Barcode Value (or leave empty to auto-generate)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.QrCode, contentDescription = null, tint = VyaparBlue) })
                        Spacer(modifier = Modifier.height(12.dp))
                        ExposedDropdownMenuBox(expanded = formatExpanded, onExpandedChange = { formatExpanded = it }) {
                            OutlinedTextField(value = selectedFormat, onValueChange = {}, readOnly = true, label = { Text("Barcode Format") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                            ExposedDropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }) {
                                formats.forEach { format -> DropdownMenuItem(text = { Text(format) }, onClick = { selectedFormat = format; formatExpanded = false }) }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            val code = barcodeValue.ifBlank { generateUniqueBarcode() }
                            val format = when (selectedFormat) {
                                "EAN13" -> BarcodeFormat.EAN_13
                                "EAN8" -> BarcodeFormat.EAN_8
                                "UPC_A" -> BarcodeFormat.UPC_A
                                "CODE39" -> BarcodeFormat.CODE_39
                                "QR_CODE" -> BarcodeFormat.QR_CODE
                                else -> BarcodeFormat.CODE_128
                            }
                            val bitmap = generateBarcodeBitmap(code, format)
                            if (bitmap != null) {
                                // Save to cache
                                val file = File(context.cacheDir, "barcode_${System.currentTimeMillis()}.png")
                                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                                generatedBarcodes = generatedBarcodes + GeneratedBarcode(code, selectedFormat, itemName, bitmap)
                                barcodeValue = ""
                                scope.launch { showPreview = true; previewBitmap = bitmap; previewCode = code }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue),
                    enabled = itemName.isNotBlank()
                ) {
                    Icon(Icons.Filled.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Barcode", fontWeight = FontWeight.Bold)
                }
            }

            if (generatedBarcodes.isNotEmpty()) {
                item { Text("Generated Barcodes (${generatedBarcodes.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary) }
            }

            items(generatedBarcodes.reversed(), key = { "${it.code}_${it.itemName}" }) { barcode ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(bitmap = barcode.bitmap.asImageBitmap(), contentDescription = "Barcode", modifier = Modifier.size(80.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(4.dp), contentScale = ContentScale.Fit)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(barcode.itemName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                            Text("${barcode.format}: ${barcode.code}", fontSize = 12.sp, color = TextSecondary)
                        }
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(File(context.cacheDir, "barcode_*.png")))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Barcode"))
                        }) { Icon(Icons.Filled.Share, contentDescription = "Share", tint = VyaparBlue) }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showPreview && previewBitmap != null) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            title = { Text("Barcode Generated!", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(bitmap = previewBitmap!!.asImageBitmap(), contentDescription = "Barcode", modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(8.dp), contentScale = ContentScale.Fit)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(previewCode, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            confirmButton = { TextButton(onClick = { showPreview = false }) { Text("Done", color = VyaparBlue, fontWeight = FontWeight.Bold) } }
        )
    }
}

private fun calculateEan13CheckDigit(data: String): Int {
    val digits = data.map { it.toString().toInt() }
    val sum = digits.mapIndexed { index, digit -> if (index % 2 == 0) digit else digit * 3 }.sum()
    return (10 - (sum % 10)) % 10
}

private fun calculateEan8CheckDigit(data: String): Int {
    val digits = data.map { it.toString().toInt() }
    val sum = digits.mapIndexed { index, digit -> if (index % 2 == 0) digit * 3 else digit }.sum()
    return (10 - (sum % 10)) % 10
}

private fun calculateUpcCheckDigit(data: String): Int {
    val digits = data.map { it.toString().toInt() }
    val sum = digits.mapIndexed { index, digit -> if (index % 2 == 0) digit * 3 else digit }.sum()
    return (10 - (sum % 10)) % 10
}

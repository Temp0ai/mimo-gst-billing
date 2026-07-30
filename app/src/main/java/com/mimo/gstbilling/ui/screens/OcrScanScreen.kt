package com.mimo.gstbilling.ui.screens

import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.OcrInvoiceParser
import com.mimo.gstbilling.utils.OcrInvoiceResult
import com.mimo.gstbilling.utils.OcrItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScanScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val scanner = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val isProcessing = remember { mutableStateOf(false) }
    val ocrText = remember { mutableStateOf("") }
    val parseResult = remember { mutableStateOf<OcrInvoiceResult?>(null) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    fun processImage(bmp: Bitmap) {
        isProcessing.value = true
        errorMessage.value = null
        ocrText.value = ""
        parseResult.value = null

        val image = InputImage.fromBitmap(bmp, 0)
        scanner.process(image)
            .addOnSuccessListener { visionText ->
                ocrText.value = visionText.text
                parseResult.value = OcrInvoiceParser.parseInvoiceText(visionText.text)
                isProcessing.value = false
            }
            .addOnFailureListener { e ->
                errorMessage.value = "Failed to scan: ${e.localizedMessage}"
                isProcessing.value = false
            }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { result ->
        result?.let {
            bitmap.value = it
            processImage(it)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                bitmap.value = bmp
                processImage(bmp)
            } catch (e: Exception) {
                errorMessage.value = "Failed to load image: ${e.localizedMessage}"
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Invoice", color = VyaparTopBarText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VyaparTopBarIcon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VyaparTopBarBackground)
            )
        },
        containerColor = VyaparBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (bitmap.value == null) {
                EmptyScannerState(
                    onTakePhoto = { cameraLauncher.launch(null) },
                    onChooseGallery = { galleryLauncher.launch("image/*") }
                )
            } else {
                Image(
                    bitmap = bitmap.value!!.asImageBitmap(),
                    contentDescription = "Invoice image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isProcessing.value) {
                    ProcessingCard()
                } else if (errorMessage.value != null) {
                    ErrorCard(
                        message = errorMessage.value!!,
                        onRetry = {
                            bitmap.value = null
                            ocrText.value = ""
                            parseResult.value = null
                            errorMessage.value = null
                        }
                    )
                } else if (parseResult.value != null) {
                    val result = parseResult.value!!

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (ocrText.value.isNotBlank()) {
                            item {
                                RecognizedTextCard(text = ocrText.value)
                            }
                        }

                        if (result.customerName.isNotBlank() || result.gstin.isNotBlank() || result.invoiceNumber.isNotBlank() || result.date.isNotBlank()) {
                            item {
                                HeaderInfoCard(
                                    customerName = result.customerName,
                                    gstin = result.gstin,
                                    invoiceNumber = result.invoiceNumber,
                                    date = result.date
                                )
                            }
                        }

                        if (result.items.isNotEmpty()) {
                            item {
                                Text(
                                    "Items Found (${result.items.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VyaparTextPrimary
                                )
                            }
                            items(result.items) { item ->
                                ItemCard(item = item)
                            }
                        }

                        if (result.totalAmount > 0) {
                            item {
                                TotalCard(amount = result.totalAmount)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            navController.previousBackStackEntry?.savedStateHandle?.apply {
                                set("ocr_customer_name", result.customerName)
                                set("ocr_gstin", result.gstin)
                                set("ocr_invoice_number", result.invoiceNumber)
                                set("ocr_date", result.date)
                                set("ocr_items_json", result.items.map { "${it.name}|${it.quantity}|${it.rate}|${it.amount}" }.joinToString(";;"))
                                set("ocr_total", result.totalAmount)
                                set("ocr_raw_text", ocrText.value)
                            }
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VyaparWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Invoice with This Data", fontWeight = FontWeight.SemiBold, color = VyaparWhite)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyScannerState(
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = VyaparBlue.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Scan an invoice to auto-fill details",
            style = MaterialTheme.typography.titleMedium,
            color = VyaparTextPrimary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Take a photo or choose from gallery",
            style = MaterialTheme.typography.bodyMedium,
            color = VyaparTextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = VyaparWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Take Photo", fontWeight = FontWeight.SemiBold, color = VyaparWhite)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onChooseGallery,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparBlue)
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose from Gallery", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProcessingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VyaparCardBackground)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = VyaparBlue,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Scanning invoice...",
                style = MaterialTheme.typography.bodyLarge,
                color = VyaparTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VyaparErrorBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VyaparErrorText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, color = VyaparErrorText, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRetry) {
                Text("Try Again", color = VyaparBlue)
            }
        }
    }
}

@Composable
private fun RecognizedTextCard(text: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VyaparCardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recognized Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = VyaparTextPrimary
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide" else "Show", color = VyaparBlue)
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = VyaparTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderInfoCard(
    customerName: String,
    gstin: String,
    invoiceNumber: String,
    date: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VyaparCardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Extracted Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VyaparTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (customerName.isNotBlank()) {
                InfoRow(label = "Customer", value = customerName)
            }
            if (gstin.isNotBlank()) {
                InfoRow(label = "GSTIN", value = gstin)
            }
            if (invoiceNumber.isNotBlank()) {
                InfoRow(label = "Invoice No", value = invoiceNumber)
            }
            if (date.isNotBlank()) {
                InfoRow(label = "Date", value = date)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = VyaparTextSecondary,
            modifier = Modifier.width(100.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = VyaparTextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ItemCard(item: OcrItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = VyaparCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name.ifBlank { "Unnamed Item" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = VyaparTextPrimary
                )
                Text(
                    "${item.quantity} x ₹${String.format("%.2f", item.rate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VyaparTextSecondary
                )
            }
            Text(
                "₹${String.format("%.2f", item.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = VyaparBlue
            )
        }
    }
}

@Composable
private fun TotalCard(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VyaparLightBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Total Amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VyaparBlue
            )
            Text(
                "₹${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VyaparBlue
            )
        }
    }
}

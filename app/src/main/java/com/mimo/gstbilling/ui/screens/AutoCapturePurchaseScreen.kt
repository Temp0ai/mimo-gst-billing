package com.mimo.gstbilling.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mimo.gstbilling.data.local.entity.InvoiceEntity
import com.mimo.gstbilling.data.local.entity.InvoiceItemEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.OcrInvoiceParser
import com.mimo.gstbilling.utils.OcrItem
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class CaptureStep {
    CAMERA, SCANNING, REVIEW, SAVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCapturePurchaseScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val invoiceState by invoiceViewModel.uiState.collectAsState()

    var currentStep by remember { mutableStateOf(CaptureStep.CAMERA) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var ocrResult by remember { mutableStateOf("") }
    var parsedInvoice by remember { mutableStateOf<com.mimo.gstbilling.utils.OcrInvoiceResult?>(null) }

    // Editable fields from OCR
    var supplierName by remember { mutableStateOf("") }
    var supplierGstin by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<OcrItem>>(emptyList()) }
    var rawText by remember { mutableStateOf("") }

    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission.value = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission.value) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentStep) {
                            CaptureStep.CAMERA -> "Capture Purchase Bill"
                            CaptureStep.SCANNING -> "Scanning..."
                            CaptureStep.REVIEW -> "Review & Save"
                            CaptureStep.SAVED -> "Saved!"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentStep) {
                            CaptureStep.REVIEW -> currentStep = CaptureStep.CAMERA
                            else -> navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        when (currentStep) {
            CaptureStep.CAMERA -> {
                CameraPreview(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    hasCameraPermission = hasCameraPermission.value,
                    onImageCaptured = { uri ->
                        capturedImageUri = uri
                        currentStep = CaptureStep.SCANNING
                        // Run OCR
                        scope.launch {
                            try {
                                val image = InputImage.fromFilePath(context, uri)
                                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                recognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        ocrResult = visionText.text
                                        val parsed = OcrInvoiceParser.parseInvoiceText(visionText.text)
                                        parsedInvoice = parsed
                                        supplierName = parsed.customerName
                                        supplierGstin = parsed.gstin
                                        invoiceNumber = parsed.invoiceNumber
                                        invoiceDate = parsed.date
                                        totalAmount = if (parsed.totalAmount > 0) String.format("%.2f", parsed.totalAmount) else ""
                                        items = parsed.items
                                        rawText = parsed.rawText
                                        currentStep = CaptureStep.REVIEW
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        currentStep = CaptureStep.CAMERA
                                    }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                currentStep = CaptureStep.CAMERA
                            }
                        }
                    },
                    onGalleryClick = {
                        // Gallery picker
                    }
                )
            }

            CaptureStep.SCANNING -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = VyaparBlue, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI is scanning your bill...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Extracting invoice details", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }

            CaptureStep.REVIEW -> {
                ReviewStep(
                    modifier = Modifier.padding(padding),
                    capturedImageUri = capturedImageUri,
                    supplierName = supplierName,
                    onSupplierNameChange = { supplierName = it },
                    supplierGstin = supplierGstin,
                    onSupplierGstinChange = { supplierGstin = it },
                    invoiceNumber = invoiceNumber,
                    onInvoiceNumberChange = { invoiceNumber = it },
                    invoiceDate = invoiceDate,
                    onInvoiceDateChange = { invoiceDate = it },
                    totalAmount = totalAmount,
                    onTotalAmountChange = { totalAmount = it },
                    items = items,
                    onItemUpdate = { index, item ->
                        items = items.toMutableList().apply { set(index, item) }
                    },
                    onItemRemove = { index ->
                        items = items.toMutableList().apply { removeAt(index) }
                    },
                    onAddItem = {
                        items = items + OcrItem(name = "", quantity = 1.0, rate = 0.0, amount = 0.0)
                    },
                    rawText = rawText,
                    onSave = {
                        // Save as purchase invoice
                        scope.launch {
                            val amount = totalAmount.toDoubleOrNull() ?: 0.0
                            if (invoiceNumber.isBlank()) {
                                Toast.makeText(context, "Please enter invoice number", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val companyId = invoiceState.companyId
                            val taxRate = 18.0
                            val taxableAmount = amount / (1 + taxRate / 100)
                            val cgst = taxableAmount * taxRate / 200
                            val sgst = taxableAmount * taxRate / 200

                            val invoice = InvoiceEntity(
                                companyId = companyId,
                                partyId = 0L,
                                invoiceNumber = invoiceNumber,
                                invoiceDate = System.currentTimeMillis(),
                                subTotal = taxableAmount,
                                taxableAmount = taxableAmount,
                                cgstTotal = cgst,
                                sgstTotal = sgst,
                                igstTotal = 0.0,
                                totalAmount = amount,
                                paymentStatus = "unpaid",
                                invoiceType = "purchase",
                                notes = "Auto-captured from bill photo"
                            )
                            invoiceViewModel.savePurchaseInvoice(invoice, items.map { item ->
                                InvoiceItemEntity(
                                    invoiceId = 0L,
                                    companyId = companyId,
                                    itemId = 0L,
                                    itemName = item.name,
                                    hsnCode = "",
                                    quantity = item.quantity,
                                    unit = "Pcs",
                                    price = item.rate,
                                    taxableAmount = item.amount / (1 + taxRate / 100),
                                    cgstAmount = item.amount / (1 + taxRate / 100) * taxRate / 200,
                                    sgstAmount = item.amount / (1 + taxRate / 100) * taxRate / 200,
                                    igstAmount = 0.0,
                                    totalAmount = item.amount
                                )
                            })
                            currentStep = CaptureStep.SAVED
                            Toast.makeText(context, "Purchase invoice saved!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            CaptureStep.SAVED -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = VyaparGreen,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Purchase Bill Saved!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VyaparGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Invoice #$invoiceNumber", fontSize = 16.sp, color = TextPrimary)
                        Text("Total: ₹${totalAmount}", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Back to Dashboard")
                            }
                            Button(
                                onClick = {
                                    currentStep = CaptureStep.CAMERA
                                    capturedImageUri = null
                                    parsedInvoice = null
                                    supplierName = ""
                                    supplierGstin = ""
                                    invoiceNumber = ""
                                    invoiceDate = ""
                                    totalAmount = ""
                                    items = emptyList()
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Capture Another")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    hasCameraPermission: Boolean,
    onImageCaptured: (Uri) -> Unit,
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    var flashEnabled by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    "Point camera at purchase bill",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash toggle
                IconButton(onClick = { flashEnabled = !flashEnabled }) {
                    Icon(
                        if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Flash",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Capture button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val file = File(
                                context.cacheDir,
                                "bill_${System.currentTimeMillis()}.jpg"
                            )
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        onImageCaptured(Uri.fromFile(file))
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        Toast.makeText(context, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }
                }

                // Gallery
                IconButton(onClick = onGalleryClick) {
                    Icon(
                        Icons.Filled.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } else {
            // No permission
            Column(
                modifier = Modifier.fillMaxSize().background(LightBlueBg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Camera permission required", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Grant camera access to scan purchase bills", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ReviewStep(
    modifier: Modifier = Modifier,
    capturedImageUri: Uri?,
    supplierName: String,
    onSupplierNameChange: (String) -> Unit,
    supplierGstin: String,
    onSupplierGstinChange: (String) -> Unit,
    invoiceNumber: String,
    onInvoiceNumberChange: (String) -> Unit,
    invoiceDate: String,
    onInvoiceDateChange: (String) -> Unit,
    totalAmount: String,
    onTotalAmountChange: (String) -> Unit,
    items: List<OcrItem>,
    onItemUpdate: (Int, OcrItem) -> Unit,
    onItemRemove: (Int) -> Unit,
    onAddItem: () -> Unit,
    rawText: String,
    onSave: () -> Unit
) {
    var showRawText by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(LightBlueBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Captured image preview
        if (capturedImageUri != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Captured Bill", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        // We can't show the actual image from URI in Compose without a content provider,
                        // but we show the raw OCR text instead
                        TextButton(onClick = { showRawText = !showRawText }) {
                            Text(if (showRawText) "Hide Raw Text" else "Show Raw OCR Text")
                        }
                        AnimatedVisibility(visible = showRawText) {
                            Text(
                                rawText.ifBlank { "No text detected" },
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(LightBlueBg, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Supplier details
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Business, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Supplier Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    }
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = onSupplierNameChange,
                        label = { Text("Supplier Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Store, contentDescription = null, tint = VyaparBlue) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = supplierGstin,
                        onValueChange = onSupplierGstinChange,
                        label = { Text("Supplier GSTIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = VyaparBlue) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Invoice details
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Receipt, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invoice Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    }
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = onInvoiceNumberChange,
                        label = { Text("Invoice Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = null, tint = VyaparBlue) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = invoiceDate,
                        onValueChange = onInvoiceDateChange,
                        label = { Text("Invoice Date") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = VyaparBlue) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = onTotalAmountChange,
                        label = { Text("Total Amount *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Text("₹", color = VyaparBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Items
        if (items.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Items (${items.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    TextButton(onClick = onAddItem) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }

            itemsIndexed(items) { index, item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Item ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VyaparBlue)
                            IconButton(onClick = { onItemRemove(index) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = RedAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { onItemUpdate(index, item.copy(name = it)) },
                            label = { Text("Item Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = item.quantity.toString(),
                                onValueChange = { onItemUpdate(index, item.copy(quantity = it.toDoubleOrNull() ?: 0.0)) },
                                label = { Text("Qty") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = item.rate.toString(),
                                onValueChange = { onItemUpdate(index, item.copy(rate = it.toDoubleOrNull() ?: 0.0)) },
                                label = { Text("Rate") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = String.format("%.2f", item.amount),
                                onValueChange = {},
                                label = { Text("Amount") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                readOnly = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onAddItem) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add items manually")
                        }
                    }
                }
            }
        }

        // Save button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparGreen)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Purchase Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

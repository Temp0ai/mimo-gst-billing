package com.mimo.gstbilling.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import com.mimo.gstbilling.utils.WatermarkType
import com.mimo.gstbilling.utils.PdfCopyType
import android.graphics.Canvas as AndroidCanvas
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePreviewScreen(
    navController: NavController,
    invoiceId: Long = 0L,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var invoice by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.InvoiceEntity?>(null) }
    var invoiceItems by remember { mutableStateOf<List<com.mimo.gstbilling.data.local.entity.InvoiceItemEntity>>(emptyList()) }
    var party by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.PartyEntity?>(null) }
    var company by remember { mutableStateOf<com.mimo.gstbilling.data.local.entity.CompanyEntity?>(null) }
    var selectedStyle by remember { mutableStateOf("CLASSIC") }
    var showStyleSheet by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        val inv = viewModel.getInvoiceByIdDirect(invoiceId)
        invoice = inv
        inv?.let {
            invoiceItems = viewModel.getItemsForInvoice(it.id)
            party = viewModel.getPartyById(it.partyId)
            company = viewModel.getCompanyById(it.companyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showStyleSheet = true }) {
                        Text("Template", color = VyaparBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    IconButton(onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(
                                context, inv, invoiceItems, company, party,
                                isThermal = false, templateStyle = selectedStyle
                            )
                            PdfGenerator.sharePdf(context, file)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color(0xFF25D366))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = VyaparTextPrimary,
                    navigationIconContentColor = VyaparTextPrimary
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparBlue)
                ) {
                    Text("Back", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(
                                context, inv, invoiceItems, company, party,
                                isThermal = false, templateStyle = selectedStyle
                            )
                            PdfGenerator.sharePdfToWhatsApp(context, file, party?.phone)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(
                                context, inv, invoiceItems, company, party,
                                isThermal = false, templateStyle = selectedStyle
                            )
                            PdfGenerator.sharePdfViaEmail(context, file, party?.email, "Invoice #${inv.invoiceNumber}")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Email", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        invoice?.let { inv ->
                            val file = PdfGenerator.generateInvoicePdf(
                                context, inv, invoiceItems, company, party,
                                isThermal = false, templateStyle = selectedStyle
                            )
                            PdfGenerator.printPdf(context, file)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                ) {
                    Text("Print", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(VyaparBackground)
        ) {
            // Template selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("CLASSIC", "MODERN", "ELEGANT", "PROFESSIONAL", "BOLD", "COMPACT", "MINIMAL", "DETAILED").forEach { style ->
                    FilterChip(
                        selected = selectedStyle == style,
                        onClick = { selectedStyle = style },
                        label = { Text(style, fontSize = 11.sp) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VyaparBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = VyaparTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = VyaparDivider,
                            selectedBorderColor = VyaparBlue,
                            enabled = true,
                            selected = selectedStyle == style
                        )
                    )
                }
            }

            // PDF Preview placeholder
            if (invoice == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VyaparBlue)
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Invoice summary
                        Text("Invoice #${invoice!!.invoiceNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                        Text("Template: $selectedStyle", fontSize = 12.sp, color = VyaparTextSecondary)
                        Text("Items: ${invoiceItems.size}", fontSize = 12.sp, color = VyaparTextSecondary)
                        Text("Total: ₹${String.format(java.util.Locale.US, "%,.2f", invoice!!.totalAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VyaparBlue)
                        Text("Status: ${invoice!!.paymentStatus.uppercase()}", fontSize = 12.sp, color = if (invoice!!.paymentStatus == "paid") VyaparGreen else VyaparRed)
                    }
                }

                // Style description
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = VyaparInfoBackground)
                ) {
                    Text(
                        when (selectedStyle) {
                            "CLASSIC" -> "Clean traditional layout with blue accents"
                            "MODERN" -> "Minimalist design with bold headers"
                            "ELEGANT" -> "Sophisticated design with subtle borders"
                            "PROFESSIONAL" -> "Corporate style with structured grid"
                            "BOLD" -> "Strong colors and large typography"
                            "COMPACT" -> "Fits more items on one page"
                            "MINIMAL" -> "Simple clean design, no borders"
                            "DETAILED" -> "Full breakdown with all fields visible"
                            else -> ""
                        },
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = VyaparInfoText
                    )
                }
            }
        }
    }

    if (showStyleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStyleSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Invoice Template", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VyaparTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                listOf(
                    "CLASSIC" to "Classic - Blue header, traditional layout",
                    "MODERN" to "Modern - Dark header band, minimalist",
                    "ELEGANT" to "Elegant - Double-border, sophisticated",
                    "PROFESSIONAL" to "Professional - Corporate blue bar",
                    "BOLD" to "Bold - Full red header, large text",
                    "COMPACT" to "Compact - Minimal spacing, dense",
                    "MINIMAL" to "Minimal - Clean text-only, no borders",
                    "DETAILED" to "Detailed - Green header, full fields"
                ).forEach { (style, description) ->
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = if (selectedStyle == style) VyaparBlue else VyaparTextPrimary,
                        fontWeight = if (selectedStyle == style) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStyle = style; showStyleSheet = false }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = VyaparDivider)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

package com.mimo.gstbilling.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import com.mimo.gstbilling.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CardTemplate(
    val id: Int,
    val name: String,
    val bgColors: List<Color>,
    val textColor: Color,
    val subTextColor: Color,
    val accentColor: Color,
    val layoutStyle: Int,
    val hasPattern: Boolean = false,
    val patternStyle: Int = 0
)

val cardTemplates = listOf(
    CardTemplate(1, "Royal Blue", listOf(Color(0xFF1A237E), Color(0xFF283593)), Color.White, Color(0xFFBBDEFB), Color(0xFFFFD54F), 0),
    CardTemplate(2, "Sunset Gold", listOf(Color(0xFFBF360C), Color(0xFFE65100)), Color.White, Color(0xFFFFCCBC), Color(0xFFFFF176), 1, hasPattern = true, patternStyle = 1),
    CardTemplate(3, "Forest Green", listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)), Color.White, Color(0xFFC8E6C9), Color(0xFFA5D6A7), 2),
    CardTemplate(4, "Royal Purple", listOf(Color(0xFF4A148C), Color(0xFF6A1B9A)), Color.White, Color(0xFFE1BEE7), Color(0xFFFFD54F), 0),
    CardTemplate(5, "Ocean Teal", listOf(Color(0xFF004D40), Color(0xFF00695C)), Color.White, Color(0xFFB2DFDB), Color(0xFF80CBC4), 1),
    CardTemplate(6, "Premium Black", listOf(Color(0xFF212121), Color(0xFF424242)), Color.White, Color(0xFFBDBDBD), Color(0xFFFFD54F), 2, hasPattern = true, patternStyle = 2),
    CardTemplate(7, "Rose Pink", listOf(Color(0xFF880E4F), Color(0xFFAD1457)), Color.White, Color(0xFFF8BBD0), Color(0xFFFCE4EC), 0),
    CardTemplate(8, "Navy Corporate", listOf(Color(0xFF0D47A1), Color(0xFF1565C0)), Color.White, Color(0xFFBBDEFB), Color(0xFF64B5F6), 1),
    CardTemplate(9, "Warm Brown", listOf(Color(0xFF3E2723), Color(0xFF5D4037)), Color(0xFFFFECB3), Color(0xFFD7CCC8), Color(0xFFFFAB91), 2),
    CardTemplate(10, "Sky Gradient", listOf(Color(0xFF0288D1), Color(0xFF26C6DA)), Color.White, Color(0xFFE1F5FE), Color(0xFFB3E5FC), 0, hasPattern = true, patternStyle = 3),
    CardTemplate(11, "Saffron Blaze", listOf(Color(0xFFE65100), Color(0xFFF57C00)), Color.White, Color(0xFFFFE0B2), Color(0xFFFFF176), 1),
    CardTemplate(12, "Maroon Gold", listOf(Color(0xFFB71C1C), Color(0xFFC62828)), Color(0xFFFFF9C4), Color(0xFFEF9A9A), Color(0xFFFFD54F), 2, hasPattern = true, patternStyle = 4),
    CardTemplate(13, "Indigo Dream", listOf(Color(0xFF1A237E), Color(0xFF3949AB)), Color.White, Color(0xFFC5CAE9), Color(0xFF9FA8DA), 0),
    CardTemplate(14, "Emerald City", listOf(Color(0xFF00695C), Color(0xFF26A69A)), Color.White, Color(0xFFB2DFDB), Color(0xFF80CBC4), 1),
    CardTemplate(15, "Charcoal Elite", listOf(Color(0xFF37474F), Color(0xFF546E7A)), Color.White, Color(0xFFB0BEC5), Color(0xFFFFD54F), 2),
    CardTemplate(16, "Ruby Red", listOf(Color(0xFFB71C1C), Color(0xFFE53935)), Color.White, Color(0xFFFFCDD2), Color(0xFFFFAB91), 0, hasPattern = true, patternStyle = 5)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCardDesignerScreen(
    navController: NavController,
    viewModel: CompanyViewModel = hiltViewModel()
) {
    val selectedCompany by viewModel.selectedCompany.collectAsState(initial = null)
    var selectedTemplate by remember { mutableStateOf(cardTemplates[0]) }
    var showPreview by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showPreview) {
        CardPreviewDialog(
            template = selectedTemplate,
            company = selectedCompany,
            onDismiss = { showPreview = false },
            onShare = { bitmap ->
                scope.launch {
                    shareBitmap(context, bitmap, "Business Card - ${selectedCompany?.name ?: "My Business"}")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Card Designer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            // Current card preview
            selectedCompany?.let { company ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = selectedTemplate.bgColors.map { it },
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val logoBitmap = remember(company.logoUri) {
                                    company.logoUri?.let { uriStr ->
                                        try {
                                            val uri = Uri.parse(uriStr)
                                            val inputStream = context.contentResolver.openInputStream(uri)
                                            val bmp = android.graphics.BitmapFactory.decodeStream(inputStream)
                                            inputStream?.close()
                                            bmp
                                        } catch (_: Exception) { null }
                                    }
                                }
                                if (logoBitmap != null) {
                                    Image(
                                        bitmap = logoBitmap.asImageBitmap(),
                                        contentDescription = "Logo",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        company.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = selectedTemplate.textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    company.businessType?.let {
                                        Text(it, fontSize = 12.sp, color = selectedTemplate.subTextColor)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            company.gstin?.let {
                                Text("GSTIN: $it", fontSize = 11.sp, color = selectedTemplate.subTextColor)
                            }
                            company.phone?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, tint = selectedTemplate.accentColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(it, fontSize = 11.sp, color = selectedTemplate.subTextColor)
                                }
                            }
                            company.email?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Email, contentDescription = null, tint = selectedTemplate.accentColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(it, fontSize = 11.sp, color = selectedTemplate.subTextColor)
                                }
                            }
                            company.address?.let {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = selectedTemplate.accentColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(it, fontSize = 10.sp, color = selectedTemplate.subTextColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                // Share button
                Button(
                    onClick = { showPreview = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share This Card", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tip text
                Text(
                    "67% businessmen saw their business increase after sharing their visiting card",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Template grid header
                Text(
                    "Choose a Design",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Template grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(cardTemplates) { index, template ->
                        TemplateCard(
                            template = template,
                            isSelected = selectedTemplate.id == template.id,
                            company = company,
                            onClick = { selectedTemplate = template }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: CardTemplate,
    isSelected: Boolean,
    company: CompanyEntity?,
    onClick: () -> Unit
) {
    val borderWidth = if (isSelected) 3.dp else 0.dp
    val borderColor = if (isSelected) Primary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = template.bgColors,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(10.dp)
        ) {
            Column {
                Text(
                    template.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = template.textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                company?.let {
                    Text(
                        it.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = template.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    it.businessType?.let { bt ->
                        Text(bt, fontSize = 9.sp, color = template.subTextColor, maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (isSelected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = template.accentColor,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun CardPreviewDialog(
    template: CardTemplate,
    company: CompanyEntity?,
    onDismiss: () -> Unit,
    onShare: (Bitmap) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    company?.let { comp ->
                        val bitmap = renderBusinessCardToBitmap(context, comp, template)
                        onShare(bitmap)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Business Card Preview", fontWeight = FontWeight.Bold) },
        text = {
            company?.let { comp ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Rendered card bitmap preview
                    val bitmap = remember(template.id, comp.id) {
                        renderBusinessCardToBitmap(context, comp, template)
                    }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Business Card",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Share this card via WhatsApp, Email, or any app",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}

fun renderBusinessCardToBitmap(context: Context, company: CompanyEntity, template: CardTemplate): Bitmap {
    val width = 1200
    val height = 750
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background gradient
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            template.bgColors.map { it.toArgb() }.toIntArray().first(),
            template.bgColors.map { it.toArgb() }.toIntArray().lastOrNull()
                ?: template.bgColors.first().toArgb(),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Pattern overlay
    if (template.hasPattern) {
        drawPattern(canvas, width, height, template)
    }

    val textColorArgb = template.textColor.toArgb()
    val subTextColorArgb = template.subTextColor.toArgb()
    val accentArgb = template.accentColor.toArgb()

    // Company logo
    var yPos = 60f
    var logoSize = 0f
    company.logoUri?.let { uriStr ->
        try {
            val uri = Uri.parse(uriStr)
            val inputStream = context.contentResolver.openInputStream(uri)
            val logoBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (logoBitmap != null) {
                logoSize = 100f
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize.toInt(), logoSize.toInt(), true)
                val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                canvas.save()
                canvas.clipRect(60f, yPos, 60f + logoSize, yPos + logoSize)
                canvas.drawBitmap(scaledLogo, 60f, yPos, logoPaint)
                canvas.restore()
                scaledLogo.recycle()
            }
        } catch (_: Exception) {}
    }

    // Company name
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColorArgb
        textSize = 52f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isFakeBoldText = true
    }
    val nameX = if (logoSize > 0) 60f + logoSize + 24f else 60f
    val nameY = if (logoSize > 0) yPos + 55f else 100f
    val displayName = company.name.take(25)
    canvas.drawText(displayName, nameX, nameY, namePaint)

    // Business type
    company.businessType?.let { bt ->
        val typePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = subTextColorArgb
            textSize = 30f
        }
        val typeY = if (logoSize > 0) yPos + 90f else 140f
        canvas.drawText(bt.take(35), nameX, typeY, typePaint)
    }

    // Divider line
    val dividerY = if (logoSize > 0) yPos + logoSize + 30f else 170f
    val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentArgb
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    canvas.drawLine(60f, dividerY, width - 60f, dividerY, dividerPaint)

    // Contact details
    var detailY = dividerY + 50f
    val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = subTextColorArgb
        textSize = 28f
    }
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentArgb
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }

    // GSTIN
    company.gstin?.let { gstin ->
        canvas.drawText("GSTIN:", 60f, detailY, iconPaint)
        canvas.drawText(gstin, 180f, detailY, detailPaint)
        detailY += 45f
    }

    // Phone
    company.phone?.let { phone ->
        canvas.drawText("PH:", 60f, detailY, iconPaint)
        canvas.drawText(phone, 180f, detailY, detailPaint)
        detailY += 45f
    }

    // Email
    company.email?.let { email ->
        canvas.drawText("EMAIL:", 60f, detailY, iconPaint)
        canvas.drawText(email.take(40), 220f, detailY, detailPaint)
        detailY += 45f
    }

    // Address
    company.address?.let { addr ->
        canvas.drawText("ADDR:", 60f, detailY, iconPaint)
        val addrPaint = Paint(detailPaint).apply { textSize = 24f }
        val addrText = addr.take(60)
        canvas.drawText(addrText, 220f, detailY, addrPaint)
    }

    // Bottom accent bar
    val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentArgb
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, height - 12f, width.toFloat(), height.toFloat(), barPaint)

    return bitmap
}

private fun drawPattern(canvas: Canvas, width: Int, height: Int, template: CardTemplate) {
    val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    val patternColor = template.accentColor.copy(alpha = 0.15f).toArgb()
    patternPaint.color = patternColor

    when (template.patternStyle) {
        1 -> {
            // Diagonal lines
            for (i in -height..width step 40) {
                canvas.drawLine(i.toFloat(), 0f, (i + height).toFloat(), height.toFloat(), patternPaint)
            }
        }
        2 -> {
            // Diamond grid
            for (x in 0..width step 60) {
                for (y in 0..height step 60) {
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 25f, patternPaint)
                }
            }
        }
        3 -> {
            // Wave lines
            val wavePaint = Paint(patternPaint).apply { strokeWidth = 2f }
            for (y in 0..height step 50) {
                val path = android.graphics.Path()
                path.moveTo(0f, y.toFloat())
                for (x in 0..width step 20) {
                    path.lineTo(x.toFloat(), y + (if (x % 40 == 0) 15f else -15f))
                }
                canvas.drawPath(path, wavePaint)
            }
        }
        4 -> {
            // Corner ornaments
            val cornerPaint = Paint(patternPaint).apply { strokeWidth = 3f }
            // Top-left
            canvas.drawArc(20f, 20f, 120f, 120f, 180f, 90f, false, cornerPaint)
            canvas.drawArc(40f, 40f, 100f, 100f, 180f, 90f, false, cornerPaint)
            // Top-right
            canvas.drawArc(width - 120f, 20f, width - 20f, 120f, 270f, 90f, false, cornerPaint)
            canvas.drawArc(width - 100f, 40f, width - 40f, 100f, 270f, 90f, false, cornerPaint)
            // Bottom-left
            canvas.drawArc(20f, height - 120f, 120f, height - 20f, 90f, 90f, false, cornerPaint)
            canvas.drawArc(40f, height - 100f, 100f, height - 40f, 90f, 90f, false, cornerPaint)
            // Bottom-right
            canvas.drawArc(width - 120f, height - 120f, width - 20f, height - 20f, 0f, 90f, false, cornerPaint)
            canvas.drawArc(width - 100f, height - 100f, width - 40f, height - 40f, 0f, 90f, false, cornerPaint)
        }
        5 -> {
            // Dots pattern
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = patternColor
                style = Paint.Style.FILL
            }
            for (x in 0..width step 30) {
                for (y in 0..height step 30) {
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 4f, dotPaint)
                }
            }
        }
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap, title: String) {
    val path = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
    val file = java.io.File(path, "Mimo_BusinessCard_${System.currentTimeMillis()}.png")
    java.io.FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Here's my business card! - ${title.removePrefix("Business Card - ")}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Business Card"))
}

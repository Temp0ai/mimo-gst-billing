package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.utils.InvoiceStyle

data class InvoiceTemplate(
    val id: String,
    val name: String,
    val description: String,
    val style: InvoiceStyle,
    val color: Color,
    val bgColor: Color,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceTemplatesScreen(navController: NavController) {
    val templates = listOf(
        InvoiceTemplate("classic", "Classic", "Clean blue accent traditional layout", InvoiceStyle.CLASSIC, Primary, Primary.copy(alpha = 0.1f), Color(0xFF0D47A1)),
        InvoiceTemplate("modern", "Modern", "Dark header minimalist design", InvoiceStyle.MODERN, Color(0xFF1A237E), Color(0xFF1A237E).copy(alpha = 0.1f), Color(0xFF283593)),
        InvoiceTemplate("elegant", "Elegant", "Sophisticated double-border style", InvoiceStyle.ELEGANT, Color(0xFF455A64), Color(0xFF455A64).copy(alpha = 0.1f), Color(0xFF37474F)),
        InvoiceTemplate("professional", "Professional", "Corporate structured with blue bar", InvoiceStyle.PROFESSIONAL, Color(0xFF0D47A1), Color(0xFF0D47A1).copy(alpha = 0.1f), Color(0xFF1565C0)),
        InvoiceTemplate("bold", "Bold", "Strong red accent large typography", InvoiceStyle.BOLD, RedAccent, RedAccent.copy(alpha = 0.1f), Color(0xFFC62828)),
        InvoiceTemplate("compact", "Compact", "Fits more items on one page", InvoiceStyle.COMPACT, GreenBalance, GreenBalance.copy(alpha = 0.1f), Color(0xFF00897B)),
        InvoiceTemplate("minimal", "Minimal", "Simple clean no-border design", InvoiceStyle.MINIMAL, TextSecondary, Color(0xFFF5F5F5), Color(0xFF757575)),
        InvoiceTemplate("detailed", "Detailed", "Full breakdown with all fields", InvoiceStyle.DETAILED, Color(0xFF1B5E20), Color(0xFF1B5E20).copy(alpha = 0.1f), Color(0xFF2E7D32))
    )

    var selectedTemplate by remember { mutableStateOf("classic") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Templates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Select a template for your invoices. Tap to preview and apply.", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(templates) { template ->
                    val isSelected = template.id == selectedTemplate
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTemplate = template.id
                                navController.previousBackStackEntry?.savedStateHandle?.set("template_id", template.id)
                                navController.previousBackStackEntry?.savedStateHandle?.set("template_style", template.style.name)
                                navController.popBackStack()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
                        border = if (isSelected) CardDefaults.outlinedCardBorder(enabled = true) else null
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(80.dp).background(template.bgColor, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(8.dp)
                                            .background(template.accentColor, RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(0.7f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                                        Box(modifier = Modifier.weight(0.5f).height(4.dp).background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(modifier = Modifier.fillMaxWidth(0.7f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                                        Box(modifier = Modifier.weight(0.5f).height(4.dp).background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(modifier = Modifier.fillMaxWidth(0.7f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                                        Box(modifier = Modifier.weight(0.5f).height(4.dp).background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                template.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Primary else TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                template.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

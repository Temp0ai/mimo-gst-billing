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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

data class InvoiceTemplate(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceTemplatesScreen(navController: NavController) {
    val templates = listOf(
        InvoiceTemplate("tax_invoice", "Tax Invoice", "Standard GST tax invoice", Icons.Filled.Receipt, Primary, Primary.copy(alpha = 0.1f)),
        InvoiceTemplate("proforma", "Proforma Invoice", "Pre-sale proforma invoice", Icons.Filled.Receipt, Color(0xFF4CAF50), Color(0xFF4CAF50).copy(alpha = 0.1f)),
        InvoiceTemplate("quotation", "Quotation", "Price quotation for customers", Icons.Filled.RequestQuote, Color(0xFFFF9800), Color(0xFFFF9800).copy(alpha = 0.1f)),
        InvoiceTemplate("estimate", "Estimate", "Cost estimate before sale", Icons.Filled.Calculate, Color(0xFF9C27B0), Color(0xFF9C27B0).copy(alpha = 0.1f)),
        InvoiceTemplate("delivery_challan", "Delivery Challan", "Goods delivery document", Icons.Filled.LocalShipping, Color(0xFF00BCD4), Color(0xFF00BCD4).copy(alpha = 0.1f)),
        InvoiceTemplate("credit_note", "Credit Note", "Return/refund document", Icons.Filled.Undo, RedAccent, RedAccent.copy(alpha = 0.1f)),
        InvoiceTemplate("debit_note", "Debit Note", "Additional charge document", Icons.Filled.NoteAdd, Color(0xFF795548), Color(0xFF795548).copy(alpha = 0.1f)),
        InvoiceTemplate("simple", "Simple Invoice", "No GST basic invoice", Icons.Filled.DocumentScanner, TextSecondary, TextSecondary.copy(alpha = 0.1f))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Templates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templates) { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.previousBackStackEntry?.savedStateHandle?.set("template_id", template.id)
                            navController.popBackStack()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(template.bgColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                template.icon,
                                contentDescription = null,
                                tint = template.color,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            template.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            template.description,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

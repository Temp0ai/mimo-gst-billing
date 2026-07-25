package com.mimo.gstbilling.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueItemDetailScreen(navController: NavController, itemId: Long) {
    val context = LocalContext.current
    var quantity by remember { mutableIntStateOf(1) }

    val productName = when (itemId) {
        1L -> "Wireless Mouse"
        2L -> "USB-C Cable"
        3L -> "Notebook Set"
        4L -> "Bluetooth Speaker"
        5L -> "Phone Case"
        6L -> "Desk Lamp"
        7L -> "Keyboard"
        8L -> "Webcam"
        else -> "Product #$itemId"
    }
    val price = when (itemId) {
        1L -> 599.0
        2L -> 299.0
        3L -> 189.0
        4L -> 1299.0
        5L -> 399.0
        6L -> 899.0
        7L -> 1599.0
        8L -> 2499.0
        else -> 0.0
    }
    val stock = when (itemId) {
        1L -> 45
        2L -> 120
        3L -> 80
        4L -> 25
        5L -> 200
        6L -> 15
        7L -> 30
        8L -> 0
        else -> 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = "Check out this product: $productName - ₹${String.format("%,.0f", price)} (${
                            if (stock > 0) "In Stock" else "Out of Stock"
                        })"
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            setPackage("com.whatsapp")
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Primary)
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
                .background(VyaparBackground)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBlueBg, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = Primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(productName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "\u20B9${String.format("%,.0f", price)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (stock > 0) GreenBalance else RedAccent, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (stock > 0) "In Stock ($stock available)" else "Out of Stock",
                                fontSize = 13.sp,
                                color = if (stock > 0) GreenBalance else RedAccent
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Description", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "High-quality $productName designed for everyday use. Made with premium materials for durability and performance. Perfect for home and office use.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Quantity", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(LightBlueBg, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = null, tint = Primary)
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                quantity.toString(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            IconButton(
                                onClick = { if (quantity < stock) quantity++ },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(LightBlueBg, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Total: \u20B9${String.format("%,.0f", price * quantity)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = { /* TODO: Add to cart */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = stock > 0
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Cart", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

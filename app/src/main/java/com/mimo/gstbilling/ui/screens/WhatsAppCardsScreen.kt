package com.mimo.gstbilling.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class WhatsAppCard(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val message: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppCardsScreen(navController: NavController) {
    val context = LocalContext.current

    val cards = listOf(
        WhatsAppCard("Greeting", Icons.Filled.CardGiftcard, "Hello! Welcome to our business. Thank you for your interest.", GreenBalance),
        WhatsAppCard("Payment\nReminder", Icons.Filled.Notifications, "Dear Customer, this is a friendly reminder about your pending payment. Please pay at your earliest convenience.", Color(0xFFFF9800)),
        WhatsAppCard("Thank You", Icons.Filled.Favorite, "Thank you for your business! We appreciate your trust in us. Looking forward to working with you again.", RedAccent),
        WhatsAppCard("Festival\nOffer", Icons.Filled.LocalOffer, "Special Festival Offer! Get exciting discounts on all our products. Limited time only!", Primary),
        WhatsAppCard("New\nProduct", Icons.Filled.NewReleases, "Check out our latest product launch! Innovative features at competitive prices.", Color(0xFF9C27B0)),
        WhatsAppCard("Order\nConfirmation", Icons.Filled.CheckCircle, "Your order has been confirmed! We will process it shortly and keep you updated.", GreenBalance)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cards) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val encoded = Uri.encode(card.message)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$encoded"))
                            context.startActivity(intent)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(card.color.copy(alpha = 0.1f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(card.icon, contentDescription = null, tint = card.color, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            card.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            card.message,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 3
                        )
                    }
                }
            }
        }
    }
}

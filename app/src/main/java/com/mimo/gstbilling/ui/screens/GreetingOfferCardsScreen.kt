package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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

data class GreetingCard(val id: String, val title: String, val emoji: String, val message: String, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingOfferCardsScreen(navController: NavController) {
    val greetings = listOf(
        GreetingCard("1", "Happy Birthday", "\uD83C\uDF82", "Wishing you a very Happy Birthday! May this year bring success and happiness.", "Birthday"),
        GreetingCard("2", "Happy Diwali", "\uD83D\uDD36", "Wishing you and your family a prosperous Diwali! May Maa Lakshmi bless you.", "Festival"),
        GreetingCard("3", "Happy New Year", "\uD83C\uDF89", "Happy New Year! May this year bring new opportunities and growth to your business.", "Festival"),
        GreetingCard("4", "Thank You", "\uD83D\uDE4F", "Thank you for your business! We truly value our partnership and look forward to serving you.", "Thank You"),
        GreetingCard("5", "Happy Holi", "\uD83C\uDF08", "Wishing you a colorful Holi! May your life be filled with happiness and success.", "Festival"),
        GreetingCard("6", "Merry Christmas", "\uD83C\uDF85", "Merry Christmas! Wishing you joy, peace, and prosperity this holiday season.", "Festival"),
        GreetingCard("7", "Happy Independence Day", "\uD83C\uDDFA\uD83C\uDDF3", "Happy Independence Day! Let's celebrate the spirit of freedom and unity.", "National"),
        GreetingCard("8", "Business Anniversary", "\uD83C\uDFC6", "Congratulations on your business anniversary! Wishing continued success and growth.", "Special")
    )
    val offers = listOf(
        GreetingCard("o1", "Flat 10% Off", "\uD83D\uDD25", "Get flat 10% discount on all orders above ₹5,000! Use code: SAVE10", "Discount"),
        GreetingCard("o2", "Buy 2 Get 1 Free", "\uD83E\uDDF3", "Buy any 2 items and get the 3rd one absolutely free! Limited period offer.", "Combo"),
        GreetingCard("o3", "Free Delivery", "\uD83D\uDCE6", "Free delivery on all orders above ₹2,000! No minimum purchase required.", "Delivery"),
        GreetingCard("o4", "Festival Sale", "\uD83C\uDF1F", "Grand festival sale! Up to 30% off on select items. Hurry, limited stock!", "Sale")
    )
    var selectedTab by remember { mutableIntStateOf(0) }
    val items = if (selectedTab == 0) greetings else offers

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Greeting & Offer Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(horizontal = 16.dp), containerColor = Color.White, contentColor = RedAccent) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Greetings") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Offers") })
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items) { card ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(card.emoji, fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(card.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(card.message, fontSize = 11.sp, color = VyaparTextSecondary, textAlign = TextAlign.Center, maxLines = 3)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 6.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                                Text("Send", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

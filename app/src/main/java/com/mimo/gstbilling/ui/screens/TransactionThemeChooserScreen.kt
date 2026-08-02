package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

data class InvoiceTheme(
    val id: String, val name: String, val primaryColor: Color, val secondaryColor: Color, val headerStyle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionThemeChooserScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("invoice_settings", Context.MODE_PRIVATE) }
    var selectedTheme by remember { mutableStateOf(prefs.getString("invoice_theme", "classic") ?: "classic") }
    val themes = listOf(
        InvoiceTheme("classic", "Classic Blue", Color(0xFF1976D2), Color(0xFFBBDEFB), "Standard"),
        InvoiceTheme("modern", "Modern Green", Color(0xFF388E3C), Color(0xFFC8E6C9), "Bold"),
        InvoiceTheme("elegant", "Elegant Red", Color(0xFFD32F2F), Color(0xFFFFCDD2), "Formal"),
        InvoiceTheme("minimal", "Minimal Gray", Color(0xFF616161), Color(0xFFE0E0E0), "Clean"),
        InvoiceTheme("premium", "Premium Gold", Color(0xFFF57F17), Color(0xFFFFF9C4), "Luxury"),
        InvoiceTheme("ocean", "Ocean Teal", Color(0xFF00796B), Color(0xFFB2DFDB), "Fresh")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Invoice Theme", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState())) {
            Text("Choose Invoice Theme", modifier = Modifier.padding(16.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(themes) { theme ->
                    Card(
                        modifier = Modifier.size(140.dp, 100.dp).border(if (selectedTheme == theme.id) 3.dp else 0.dp, if (selectedTheme == theme.id) theme.primaryColor else Color.Transparent, RoundedCornerShape(16.dp)).clickable { selectedTheme = theme.id },
                        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(theme.primaryColor, RoundedCornerShape(4.dp)))
                            Text(theme.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.primaryColor)
                            Text(theme.headerStyle, fontSize = 10.sp, color = VyaparTextSecondary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    val selected = themes.first { it.id == selectedTheme }
                    Column(modifier = Modifier.fillMaxWidth().background(selected.secondaryColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().background(selected.primaryColor, RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text(selected.name.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Invoice #INV-0001", fontSize = 13.sp, color = TextPrimary)
                        Text("Date: ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date())}", fontSize = 12.sp, color = VyaparTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Item 1 .......... ₹1,000", fontSize = 12.sp)
                        Text("Item 2 .......... ₹2,000", fontSize = 12.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Total: ₹3,000", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = selected.primaryColor)
                    }
                }
            }
            Button(onClick = {
                prefs.edit().putString("invoice_theme", selectedTheme).apply()
                navController.popBackStack()
            }, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                Text("Apply Theme", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) {
                        Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mimo GST Billing", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Version 1.0.0 (Build 1)", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("© 2026 Mimo Technologies", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AboutRow(icon = Icons.Filled.Description, title = "Terms of Service", subtitle = "Read our terms and conditions")
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                    AboutRow(icon = Icons.Filled.PrivacyTip, title = "Privacy Policy", subtitle = "How we handle your data")
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                    AboutRow(icon = Icons.Filled.Code, title = "Open Source Licenses", subtitle = "Third-party libraries used")
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
                    AboutRow(icon = Icons.Filled.Info, title = "Licenses", subtitle = "Apache License 2.0")
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mimo GST Billing", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Text("A complete GST billing solution for Indian businesses", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

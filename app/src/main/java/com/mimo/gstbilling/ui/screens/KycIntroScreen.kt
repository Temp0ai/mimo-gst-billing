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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycIntroScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KYC Verification", fontWeight = FontWeight.Bold) },
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
                .background(LightBlueBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Complete Your KYC",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Verify your business identity to unlock all features including GST filing, e-Way bills, and higher transaction limits.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Benefits of KYC", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    BenefitRow(icon = Icons.Filled.Receipt, title = "GST Filing", desc = "File GSTR-1, GSTR-3B directly from the app")
                    BenefitRow(icon = Icons.Filled.LocalShipping, title = "e-Way Bill", desc = "Generate e-Way bills for shipments above ₹50,000")
                    BenefitRow(icon = Icons.Filled.TrendingUp, title = "Higher Limits", desc = "Remove transaction limits on your account")
                    BenefitRow(icon = Icons.Filled.Shield, title = "Trust Badge", desc = "Get a verified badge on your business profile")
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("KYC Steps", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    StepIndicator(step = 1, title = "PAN Verification", status = "required")
                    StepIndicator(step = 2, title = "GSTIN / Business Proof", status = "required")
                    StepIndicator(step = 3, title = "Bank Account", status = "optional")
                    StepIndicator(step = 4, title = "Aadhaar (e-KYC)", status = "optional")
                }
            }

            Button(
                onClick = { navController.navigate(Screen.KycVerification.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Start KYC", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Text(desc, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun StepIndicator(
    step: Int,
    title: String,
    status: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when (status) {
                        "completed" -> GreenBalance
                        else -> Primary.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (status == "completed") {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text("$step", fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Text(
                when (status) {
                    "required" -> "Required"
                    "optional" -> "Optional"
                    else -> "Completed"
                },
                fontSize = 12.sp,
                color = when (status) {
                    "required" -> RedAccent
                    "completed" -> GreenBalance
                    else -> TextSecondary
                }
            )
        }
    }
}

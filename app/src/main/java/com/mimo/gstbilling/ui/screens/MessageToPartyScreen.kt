package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageToPartyScreen(navController: NavController) {
    var message by remember {
        mutableStateOf(
            "Dear Rahul Enterprises, your invoice #INV-005 for \u20B915,000 is overdue. " +
                    "Please clear the payment at the earliest."
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Message to Party", fontWeight = FontWeight.Bold) },
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
                .background(VyaparBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(LightBlueBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Business, contentDescription = null, tint = Primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Rahul Enterprises", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Outstanding: \u20B915,000", fontSize = 13.sp, color = RedAccent)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Message", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text("${message.length} characters", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparButtonGreen)
            ) {
                Icon(Icons.Filled.Sms, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send via SMS", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparButtonGreen)
            ) {
                Icon(Icons.Filled.Chat, contentDescription = null, tint = VyaparButtonGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send via WhatsApp", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        }
    }
}

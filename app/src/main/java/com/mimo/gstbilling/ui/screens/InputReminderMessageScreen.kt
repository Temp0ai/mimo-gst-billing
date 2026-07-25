package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputReminderMessageScreen(navController: NavController) {
    var message by remember {
        mutableStateOf(
            "Dear Customer, this is a friendly reminder that your payment of {amount} is overdue. " +
                    "Please clear your outstanding at the earliest. Thank you!"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder Message", fontWeight = FontWeight.Bold) },
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Filled.People, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recipients", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("4 parties selected", fontSize = 14.sp, color = TextSecondary)
                    Text(
                        "Total outstanding: \u20B950,700",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RedAccent
                    )
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
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            "${message.length} characters",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

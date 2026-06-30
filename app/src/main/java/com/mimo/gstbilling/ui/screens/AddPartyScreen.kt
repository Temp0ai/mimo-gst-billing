package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartyScreen(navController: NavController) {
    var partyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var partyType by remember { mutableIntStateOf(0) }

    val partyTypes = listOf("Customer", "Supplier", "Both")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Party", fontWeight = FontWeight.Bold)
                },
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Party Name *",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = partyName,
                            onValueChange = { partyName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter party name") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Text(
                            text = "Phone",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter phone number") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Text(
                            text = "Email",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter email address") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Text(
                            text = "GSTIN",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter GSTIN number") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Text(
                            text = "Address",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter address") },
                            shape = RoundedCornerShape(8.dp),
                            minLines = 2
                        )

                        Text(
                            text = "State",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter state") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Party Type",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            partyTypes.forEachIndexed { index, type ->
                                val isSelected = partyType == index
                                Button(
                                    onClick = { partyType = index },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Primary else Color.LightGray.copy(alpha = 0.3f),
                                        contentColor = if (isSelected) Color.White else TextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBalance),
                    shape = RoundedCornerShape(10.dp),
                    enabled = partyName.isNotEmpty()
                ) {
                    Text(
                        text = "Save Party",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

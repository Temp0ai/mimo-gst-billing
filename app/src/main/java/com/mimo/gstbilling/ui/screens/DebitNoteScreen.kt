package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.DebitNoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebitNoteScreen(
    navController: NavController,
    viewModel: DebitNoteViewModel = hiltViewModel()
) {
    val debitNotes by viewModel.debitNotes.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debit Notes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "debit_note")) },
                containerColor = RedAccent
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Debit Note", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F6F6))
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search debit notes", fontSize = 13.sp, color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            val filteredNotes = if (searchQuery.isEmpty()) debitNotes else
                debitNotes.filter { it.invoiceNumber.contains(searchQuery, ignoreCase = true) }

            if (filteredNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No debit notes yet", fontSize = 16.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to create a debit note", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(filteredNotes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { navController.navigate(Screen.InvoiceDetail.createRoute(note.id)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color(0xFFFF6B00).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("DN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B00))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(note.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(dateFormat.format(Date(note.invoiceDate)), fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    String.format(Locale.US, "\u20B9%,.2f", note.totalAmount),
                                    fontWeight = FontWeight.Bold, color = Color(0xFFFF6B00), fontSize = 14.sp
                                )
                                Text(
                                    note.paymentStatus.replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    color = if (note.paymentStatus == "paid") GreenBalance else Color(0xFFFF6B00)
                                )
                            }
                            IconButton(onClick = {
                                navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "debit_note"))
                            }) {
                                Icon(Icons.Filled.Link, contentDescription = "Link to Invoice", tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

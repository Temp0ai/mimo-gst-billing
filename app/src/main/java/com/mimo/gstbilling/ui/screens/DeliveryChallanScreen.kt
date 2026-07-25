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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.DeliveryChallanViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryChallanScreen(
    navController: NavController,
    viewModel: DeliveryChallanViewModel = hiltViewModel()
) {
    val challans by viewModel.challans.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var expandedMenuId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Delivery Challan", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this delivery challan?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteChallan(it) }
                    showDeleteDialog = null
                }) { Text("Delete", color = RedAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Challans", fontWeight = FontWeight.Bold) },
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
                onClick = { navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "delivery_challan")) },
                containerColor = RedAccent
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Delivery Challan", tint = Color.White)
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
                        placeholder = { Text("Search delivery challans", fontSize = 13.sp, color = TextSecondary) },
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

            val filteredChallans = if (searchQuery.isEmpty()) challans else
                challans.filter { it.invoiceNumber.contains(searchQuery, ignoreCase = true) }

            if (filteredChallans.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No delivery challans yet", fontSize = 16.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to create a delivery challan", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(filteredChallans) { challan ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { navController.navigate(Screen.InvoiceDetail.createRoute(challan.id)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color(0xFF1A237E).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("DC", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(challan.invoiceNumber, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(dateFormat.format(Date(challan.invoiceDate)), fontSize = 12.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    String.format(Locale.US, "\u20B9%,.2f", challan.totalAmount),
                                    fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp
                                )
                                Text(
                                    challan.paymentStatus.replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    color = if (challan.paymentStatus == "paid") GreenBalance else Primary
                                )
                            }
                            Box {
                                IconButton(onClick = { expandedMenuId = challan.id }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Actions", tint = TextSecondary)
                                }
                                DropdownMenu(
                                    expanded = expandedMenuId == challan.id,
                                    onDismissRequest = { expandedMenuId = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Convert to Invoice") },
                                        onClick = {
                                            expandedMenuId = null
                                            navController.navigate(Screen.CreateInvoice.createRoute(invoiceType = "sales"))
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Receipt, contentDescription = null, tint = Primary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Print / PDF") },
                                        onClick = {
                                            expandedMenuId = null
                                            scope.launch {
                                                val format = PdfGenerator.getPrintFormat(context)
                                                val isThermal = PdfGenerator.isThermal(format)
                                                val file = PdfGenerator.generateInvoicePdf(context, challan, viewModel.getItemsForChallan(challan.id), null, null, isThermal = isThermal)
                                                PdfGenerator.printPdf(context, file)
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Print, contentDescription = null, tint = Primary) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share PDF") },
                                        onClick = {
                                            expandedMenuId = null
                                            scope.launch {
                                                val format = PdfGenerator.getPrintFormat(context)
                                                val isThermal = PdfGenerator.isThermal(format)
                                                val file = PdfGenerator.generateInvoicePdf(context, challan, viewModel.getItemsForChallan(challan.id), null, null, isThermal = isThermal)
                                                PdfGenerator.sharePdf(context, file)
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null, tint = GreenBalance) }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = RedAccent) },
                                        onClick = {
                                            expandedMenuId = null
                                            showDeleteDialog = challan.id
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAccent) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

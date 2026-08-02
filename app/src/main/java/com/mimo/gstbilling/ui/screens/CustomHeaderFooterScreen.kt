package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomHeaderFooterScreen(navController: NavController) {
    var headerText by remember { mutableStateOf("TAX INVOICE") }
    var footerText by remember { mutableStateOf("Thank you for your business!") }
    var showHeaderPreview by remember { mutableStateOf(false) }
    var includeBankDetails by remember { mutableStateOf(true) }
    var includeTerms by remember { mutableStateOf(true) }
    var includeSignature by remember { mutableStateOf(true) }
    var customNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Header & Footer Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Invoice Header", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        OutlinedTextField(value = headerText, onValueChange = { headerText = it }, label = { Text("Header Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Text("This appears at the top of every invoice", fontSize = 12.sp, color = VyaparTextSecondary)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Invoice Footer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        OutlinedTextField(value = footerText, onValueChange = { footerText = it }, label = { Text("Footer Message") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Additional Sections", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) { Text("Bank Details", fontSize = 14.sp); Text("Show bank account info", fontSize = 12.sp, color = VyaparTextSecondary) }
                            Switch(checked = includeBankDetails, onCheckedChange = { includeBankDetails = it })
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) { Text("Terms & Conditions", fontSize = 14.sp); Text("Show terms at bottom", fontSize = 12.sp, color = VyaparTextSecondary) }
                            Switch(checked = includeTerms, onCheckedChange = { includeTerms = it })
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) { Text("Authorized Signatory", fontSize = 14.sp); Text("Show signature line", fontSize = 12.sp, color = VyaparTextSecondary) }
                            Switch(checked = includeSignature, onCheckedChange = { includeSignature = it })
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Custom Note", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        OutlinedTextField(value = customNote, onValueChange = { customNote = it }, label = { Text("Add custom note to invoices") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }
                }
            }
            item {
                Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                    Text("Save Settings", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

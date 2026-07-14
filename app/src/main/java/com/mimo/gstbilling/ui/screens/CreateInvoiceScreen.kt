package com.mimo.gstbilling.ui.screens

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.provider.ContactsContract
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalTextStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.BlueHeader
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.LightBlueBg
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import kotlinx.coroutines.launch

data class PhoneContact(val name: String, val phone: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var partyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var invoiceNo by remember { mutableStateOf(uiState.invoiceNumber) }
    var invoiceDate by remember { mutableStateOf("11/07/2026") }
    var igstEnabled by remember { mutableStateOf(false) }
    var discount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var partyMenuExpanded by remember { mutableStateOf(false) }
    var showItemPicker by remember { mutableStateOf(false) }
    var isCashSale by remember { mutableStateOf(false) }
    var partySelected by remember { mutableStateOf(false) }
    var invoicePrefix by remember { mutableStateOf("INV") }
    var invoiceNumberPart by remember { mutableStateOf("0001") }
    var showInvoicePrefixEdit by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var showPhoneContacts by remember { mutableStateOf(false) }
    var showQuickAddItem by remember { mutableStateOf(false) }
    var quickItemName by remember { mutableStateOf("") }
    var quickItemPrice by remember { mutableStateOf("") }
    var quickItemGst by remember { mutableStateOf("18") }
    var quickItemHsn by remember { mutableStateOf("") }
    var quickItemUnit by remember { mutableStateOf("Pcs") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var phoneContacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
    var allPhoneContacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }

    val contactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            allPhoneContacts = readPhoneContacts(context)
        }
    }

    val filteredPhoneContacts = if (customerSearchQuery.length >= 2) {
        allPhoneContacts.filter {
            it.name.contains(customerSearchQuery, ignoreCase = true) ||
            it.phone.contains(customerSearchQuery, ignoreCase = true)
        }
    } else emptyList()

    LaunchedEffect(customerSearchQuery) {
        if (customerSearchQuery.length >= 2 && uiState.allParties.none { it.name.contains(customerSearchQuery, ignoreCase = true) }) {
            if (allPhoneContacts.isEmpty()) {
                contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    showPhoneContacts = customerSearchQuery.length >= 2 && filteredPhoneContacts.isNotEmpty()

    val filteredParties = if (customerSearchQuery.isEmpty()) {
        uiState.allParties
    } else {
        uiState.allParties.filter {
            it.name.contains(customerSearchQuery, ignoreCase = true) ||
            it.phone?.contains(customerSearchQuery, ignoreCase = true) == true
        }
    }

    LaunchedEffect(uiState.invoiceNumber) {
        val parts = uiState.invoiceNumber.split("-")
        if (parts.size >= 2) {
            invoicePrefix = parts.dropLast(1).joinToString("-")
            invoiceNumberPart = parts.last()
        }
        invoiceNo = uiState.invoiceNumber
    }

    LaunchedEffect(invoicePrefix, invoiceNumberPart) {
        invoiceNo = "$invoicePrefix-$invoiceNumberPart"
        viewModel.updateInvoiceNumber(invoiceNo)
    }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String>("template_id", "")?.collect { templateId ->
            if (templateId.isNotBlank()) {
                viewModel.setTemplate(templateId)
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("template_id")
            }
        }
    }

    LaunchedEffect(uiState.savedInvoiceId) {
        if (uiState.savedInvoiceId != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Invoice saved!")
            }
            viewModel.resetState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Sale", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val partyLabel = if (isCashSale) "Cash" else uiState.partyName.ifBlank { "Customer" }
                        val itemsSummary = uiState.items.joinToString("\n") { item ->
                            "${item.itemName} x${item.quantity.toInt()} = ${String.format(java.util.Locale.US, "\u20B9%,.2f", item.totalAmount)}"
                        }
                        val billText = buildString {
                            appendLine(" invoice from ${uiState.partyName.ifBlank { "My Business" }}")
                            appendLine("Invoice: ${uiState.invoiceNumber}")
                            appendLine("Date: $invoiceDate")
                            appendLine("Party: $partyLabel")
                            appendLine("---")
                            appendLine(itemsSummary)
                            appendLine("---")
                            appendLine("Subtotal: ${String.format(java.util.Locale.US, "\u20B9%,.2f", uiState.items.sumOf { it.taxableAmount })}")
                            appendLine("Tax: ${String.format(java.util.Locale.US, "\u20B9%,.2f", uiState.cgstTotal + uiState.sgstTotal + uiState.igstTotal)}")
                            appendLine("Total: ${String.format(java.util.Locale.US, "\u20B9%,.2f", uiState.totalAmount)}")
                            appendLine("Payment: ${if (isCashSale) "Cash" else "Credit"}")
                            appendLine("Thank you!")
                        }
                        val encoded = Uri.encode(billText)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$encoded"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share on WhatsApp", tint = Color.White)
                    }
                    Row(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    ) {
                        FilterChip(
                            selected = !isCashSale,
                            onClick = { isCashSale = false },
                            label = { Text("Credit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenBalance,
                                selectedLabelColor = Color.White,
                                containerColor = Color.Transparent,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        FilterChip(
                            selected = isCashSale,
                            onClick = { isCashSale = true },
                            label = { Text("Cash", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Primary,
                                containerColor = Color.Transparent,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                ) {
                    Text("Cancel", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        val canSave = (isCashSale || uiState.partyId > 0L) && uiState.items.isNotEmpty()
                        if (canSave) {
                            viewModel.saveInvoice()
                        } else {
                            scope.launch {
                                val msg = if (!isCashSale && uiState.partyId <= 0L) "Select a customer first"
                                else if (uiState.items.isEmpty()) "Add at least one item"
                                else "Please fill all required fields"
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBalance),
                    enabled = !uiState.isSaving
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save & Print", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightBlueBg)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Invoice No.", fontSize = 12.sp, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = invoicePrefix,
                                onValueChange = { invoicePrefix = it },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Primary
                                )
                            )
                            Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = invoiceNumberPart,
                                onValueChange = { invoiceNumberPart = it },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Primary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { invoiceNumberPart = "" },
                                tint = TextSecondary
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Date", fontSize = 12.sp, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = invoiceDate,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Date", modifier = Modifier.size(16.dp), tint = TextSecondary)
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = customerSearchQuery,
                        onValueChange = {
                            customerSearchQuery = it
                            partyName = it
                            partySelected = false
                            if (it.isEmpty()) {
                                viewModel.setParty(0L, "", "")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Customer *") },
                        placeholder = { Text("Type name or phone number") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!partySelected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (customerSearchQuery.isEmpty()) "Showing Saved Parties" else "Showing from phone Book",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Add new party",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.AddParty.route)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (filteredParties.isEmpty() && !showPhoneContacts) {
                            Text(
                                text = if (customerSearchQuery.isEmpty()) "No saved parties yet" else "No matching contacts found",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (filteredParties.isNotEmpty()) {
                            filteredParties.forEach { party ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            partyName = party.name
                                            phone = party.phone ?: ""
                                            customerSearchQuery = party.name
                                            partySelected = true
                                            viewModel.setParty(party.id, party.name, party.phone ?: "")
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(party.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        if (!party.phone.isNullOrBlank()) {
                                            Text(party.phone, fontSize = 12.sp, color = TextSecondary)
                                        }
                                    }
                                    if (party.balance != 0.0) {
                                        Text(
                                            text = String.format(java.util.Locale.US, "\u20B9%,.2f", kotlin.math.abs(party.balance)),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (party.balance > 0) GreenBalance else RedAccent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        if (showPhoneContacts && filteredPhoneContacts.isNotEmpty()) {
                            filteredPhoneContacts.forEach { contact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                val partyId = viewModel.createPartyFromContact(contact.name, contact.phone)
                                                partyName = contact.name
                                                phone = contact.phone
                                                customerSearchQuery = contact.name
                                                partySelected = true
                                                viewModel.setParty(partyId, contact.name, contact.phone)
                                                showPhoneContacts = false
                                            }
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(contact.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text(contact.phone, fontSize = 12.sp, color = TextSecondary)
                                    }
                                    Text("0.00", fontSize = 14.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(partyName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                if (phone.isNotBlank()) {
                                    Text(phone, fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Change party",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        partySelected = false
                                        customerSearchQuery = ""
                                        partyName = ""
                                        phone = ""
                                        viewModel.setParty(0L, "", "")
                                    },
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenBalance)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Billed Items", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("Rate exl. tax", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }

            if (uiState.items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No items added yet", fontSize = 14.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select items from your inventory", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                itemsIndexed(uiState.items) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}  ${item.itemName}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "\u20B9%,.2f", item.totalAmount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Item Subtotal", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    String.format(java.util.Locale.US, "%.0f %s x %.2f = \u20B9%,.2f", item.quantity, item.unit, item.price, item.taxableAmount),
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tax GST@${item.gstRate.toInt()}%: ${item.gstRate.toInt()}%", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    String.format(java.util.Locale.US, "\u20B9%,.2f", item.cgstAmount + item.sgstAmount),
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { viewModel.removeItem(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = RedAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showItemPicker = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Add Items", fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("(Optional)", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = igstEnabled,
                                onCheckedChange = { igstEnabled = it },
                                colors = CheckboxDefaults.colors(checkedColor = Primary)
                            )
                            Text(
                                text = "IGST (Inter-State)",
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = discount,
                            onValueChange = { discount = it; viewModel.updateDiscount(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Discount") },
                            placeholder = { Text("Enter discount amount") },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it; viewModel.updateNotes(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Notes") },
                            placeholder = { Text("Add any notes") },
                            shape = RoundedCornerShape(16.dp),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontSize = 14.sp, color = TextSecondary)
                            Text(String.format(java.util.Locale.US, "\u20B9%,.2f", uiState.items.sumOf { it.taxableAmount }), fontSize = 14.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Tax:", fontSize = 14.sp, color = TextSecondary)
                            Text(String.format(java.util.Locale.US, "\u20B9%,.2f", uiState.cgstTotal + uiState.sgstTotal + uiState.igstTotal), fontSize = 14.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(String.format(java.util.Locale.US, "\u20B9%,.2f", uiState.totalAmount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlueHeader)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showItemPicker && !showQuickAddItem) {
        AlertDialog(
            onDismissRequest = { showItemPicker = false },
            title = { Text("Select Item", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showQuickAddItem = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenBalance.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Add New Item", fontWeight = FontWeight.Bold, color = GreenBalance, fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.allItems.isEmpty()) {
                        Text("No items found. Create one using the button above.", fontSize = 13.sp, color = TextSecondary)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                            items(uiState.allItems.size) { index ->
                                val item = uiState.allItems[index]
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.addItem(item)
                                        showItemPicker = false
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            Text("HSN: ${item.hsnCode ?: "N/A"} | GST: ${item.gstRate.toInt()}%", fontSize = 12.sp, color = TextSecondary)
                                        }
                                        Text(String.format(java.util.Locale.US, "\u20B9%.2f", item.salePrice), fontWeight = FontWeight.Bold, color = Primary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showItemPicker = false }) { Text("Cancel") }
            }
        )
    }

    if (showQuickAddItem) {
        var quickItemQty by remember { mutableStateOf("1") }
        var quickItemTaxIncluded by remember { mutableStateOf(false) }
        var quickItemUnitExpanded by remember { mutableStateOf(false) }
        val units = listOf("Pcs", "Kg", "Gm", "Ltr", "Mtr", "Sqm", "Box", "Pair", "Set", "Doz", "Btl", "Bag", "Roll", "Bundle", "Pack", "Nos")

        AlertDialog(
            onDismissRequest = { showQuickAddItem = false; showItemPicker = false },
            title = { Text("Add Items to Sale", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = quickItemName,
                        onValueChange = { quickItemName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Item Name") },
                        placeholder = { Text("e.g. Chocolate Cake") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0))
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = quickItemQty,
                            onValueChange = { quickItemQty = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Quantity") },
                            placeholder = { Text("1") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0))
                        )

                        ExposedDropdownMenuBox(
                            expanded = quickItemUnitExpanded,
                            onExpandedChange = { quickItemUnitExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = quickItemUnit,
                                onValueChange = {},
                                modifier = Modifier.weight(1f).menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                label = { Text("Unit") },
                                shape = RoundedCornerShape(12.dp),
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quickItemUnitExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0))
                            )
                            ExposedDropdownMenu(
                                expanded = quickItemUnitExpanded,
                                onDismissRequest = { quickItemUnitExpanded = false }
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = { quickItemUnit = unit; quickItemUnitExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = quickItemPrice,
                            onValueChange = { quickItemPrice = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Rate (Price/Unit)") },
                            placeholder = { Text("0.00") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0))
                        )

                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                FilterChip(
                                    selected = !quickItemTaxIncluded,
                                    onClick = { quickItemTaxIncluded = false },
                                    label = { Text("Without Tax", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = quickItemTaxIncluded,
                                    onClick = { quickItemTaxIncluded = true },
                                    label = { Text("With Tax", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showQuickAddItem = false; showItemPicker = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Text("Save & New", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val name = quickItemName.trim()
                            val price = quickItemPrice.toDoubleOrNull() ?: 0.0
                            val gst = quickItemGst.toDoubleOrNull() ?: 18.0
                            if (name.isNotBlank() && price > 0) {
                                scope.launch {
                                    val newItem = viewModel.createQuickItem(name, price, gst, quickItemHsn.trim(), quickItemUnit)
                                    viewModel.addItem(newItem)
                                    quickItemName = ""
                                    quickItemPrice = ""
                                    quickItemQty = "1"
                                    quickItemGst = "18"
                                    quickItemHsn = ""
                                    showQuickAddItem = false
                                    showItemPicker = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        enabled = quickItemName.isNotBlank() && (quickItemPrice.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        )
    }
}

private fun readPhoneContacts(context: android.content.Context): List<PhoneContact> {
    val contacts = mutableListOf<PhoneContact>()
    try {
        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: continue
                val number = it.getString(phoneIdx) ?: continue
                contacts.add(PhoneContact(name, number))
            }
        }
    } catch (_: Exception) {
    }
    return contacts
}

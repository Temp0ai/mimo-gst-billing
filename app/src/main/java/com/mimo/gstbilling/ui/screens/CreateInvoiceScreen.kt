package com.mimo.gstbilling.ui.screens

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.ui.viewmodel.InvoiceViewModel
import com.mimo.gstbilling.utils.PdfGenerator
import kotlinx.coroutines.launch

data class PhoneContact(val name: String, val phone: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    navController: NavController,
    preselectedPartyId: Long? = null,
    invoiceType: String = "sales",
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var partyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var invoiceNo by remember { mutableStateOf(uiState.invoiceNumber) }
    var invoiceDate by remember {
        val cal = java.util.Calendar.getInstance()
        mutableStateOf(String.format(java.util.Locale.US, "%02d/%02d/%04d", cal.get(java.util.Calendar.DAY_OF_MONTH), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR)))
    }
    var isCashSale by remember { mutableStateOf(false) }
    var partySelected by remember { mutableStateOf(false) }
    var invoicePrefix by remember { mutableStateOf("INV") }
    var invoiceNumberPart by remember { mutableStateOf("0001") }
    var customerSearchQuery by remember { mutableStateOf("") }
    var showPhoneContacts by remember { mutableStateOf(false) }
    var ewayBillNo by remember { mutableStateOf("") }

    var showSettingsPanel by remember { mutableStateOf(false) }
    var salePrefix by remember { mutableStateOf("a") }
    var salePrefixEnabled by remember { mutableStateOf(true) }
    var transactionSms by remember { mutableStateOf(true) }
    var cashSaleDefault by remember { mutableStateOf(false) }
    var showInvoiceNumber by remember { mutableStateOf(true) }
    var showBillingName by remember { mutableStateOf(false) }
    var showPoDetails by remember { mutableStateOf(false) }
    var showTimeOnTransactions by remember { mutableStateOf(false) }
    var allowInclusiveTax by remember { mutableStateOf(true) }
    var showPurchasePrice by remember { mutableStateOf(false) }
    var showLastSalePrice by remember { mutableStateOf(false) }
    var freeItemQuantity by remember { mutableStateOf(false) }
    var showCount by remember { mutableStateOf(false) }
    var barcodeScanning by remember { mutableStateOf(false) }
    var billingType by remember { mutableStateOf("full_sale") }

    LaunchedEffect(invoiceType) { viewModel.setInvoiceType(invoiceType) }
    val isPurchase = invoiceType == "purchase"
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var phoneContacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
    var allPhoneContacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }

    if (preselectedPartyId != null) {
        LaunchedEffect(preselectedPartyId, uiState.allParties) {
            if (uiState.allParties.isNotEmpty() && !partySelected) {
                val party = uiState.allParties.find { it.id == preselectedPartyId }
                if (party != null) {
                    partyName = party.name; phone = party.phone ?: ""; customerSearchQuery = party.name
                    partySelected = true; viewModel.setParty(party.id, party.name, party.phone ?: "")
                }
            }
        }
    }

    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) allPhoneContacts = readPhoneContacts(context)
    }

    val filteredPhoneContacts = if (customerSearchQuery.length >= 2) {
        allPhoneContacts.filter { it.name.contains(customerSearchQuery, ignoreCase = true) || it.phone.contains(customerSearchQuery, ignoreCase = true) }
    } else emptyList()

    LaunchedEffect(customerSearchQuery) {
        if (customerSearchQuery.length >= 2 && uiState.allParties.none { it.name.contains(customerSearchQuery, ignoreCase = true) }) {
            if (allPhoneContacts.isEmpty()) contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    showPhoneContacts = customerSearchQuery.length >= 2 && filteredPhoneContacts.isNotEmpty()

    val filteredParties = if (customerSearchQuery.isEmpty()) uiState.allParties
    else uiState.allParties.filter { it.name.contains(customerSearchQuery, ignoreCase = true) || it.phone?.contains(customerSearchQuery, ignoreCase = true) == true }

    LaunchedEffect(uiState.invoiceNumber) {
        val parts = uiState.invoiceNumber.split("-")
        if (parts.size >= 2) { invoicePrefix = parts.dropLast(1).joinToString("-"); invoiceNumberPart = parts.last() }
        invoiceNo = uiState.invoiceNumber
    }
    LaunchedEffect(invoicePrefix, invoiceNumberPart) {
        invoiceNo = "$invoicePrefix-$invoiceNumberPart"; viewModel.updateInvoiceNumber(invoiceNo)
    }

    var savedInvoiceId by remember { mutableStateOf<Long?>(null) }
    var showPostSaveDialog by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.savedInvoiceId) {
        if (uiState.savedInvoiceId != null) { savedInvoiceId = uiState.savedInvoiceId; showPostSaveDialog = true; viewModel.clearSavedInvoiceId() }
    }

    if (showPostSaveDialog && savedInvoiceId != null) {
        AlertDialog(
            onDismissRequest = { showPostSaveDialog = false; viewModel.resetState(); navController.popBackStack() },
            title = { Text("Invoice Saved!", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column {
                    Text("What would you like to do?", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            showPostSaveDialog = false; scope.launch {
                                val inv = viewModel.getInvoiceByIdDirect(savedInvoiceId!!)
                                if (inv != null) { val items = viewModel.getItemsForInvoice(inv.id); val file = PdfGenerator.generateInvoicePdf(context, inv, items, null, isThermal = false); PdfGenerator.sharePdf(context, file) }
                                viewModel.resetState(); navController.popBackStack()
                            }
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                            Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(onClick = {
                            showPostSaveDialog = false; scope.launch {
                                val inv = viewModel.getInvoiceByIdDirect(savedInvoiceId!!)
                                if (inv != null) { val items = viewModel.getItemsForInvoice(inv.id); val file = PdfGenerator.generateInvoicePdf(context, inv, items, null, isThermal = false); PdfGenerator.printPdf(context, file) }
                                viewModel.resetState(); navController.popBackStack()
                            }
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                            Text("View PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showPostSaveDialog = false; viewModel.resetState(); navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Skip", color = TextSecondary)
                    }
                }
            },
            confirmButton = {}
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isPurchase) "Purchase" else "Sale", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                    actions = {
                        Row(modifier = Modifier.padding(end = 4.dp).background(Color(0xFFF0F0F0), RoundedCornerShape(20.dp))) {
                            FilterChip(selected = !isCashSale, onClick = { isCashSale = false }, label = { Text("Credit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenBalance, selectedLabelColor = Color.White, containerColor = Color.Transparent, labelColor = TextSecondary))
                            FilterChip(selected = isCashSale, onClick = { isCashSale = true }, label = { Text("Cash", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White, selectedLabelColor = Primary, containerColor = Color.Transparent, labelColor = TextSecondary))
                        }
                        IconButton(onClick = { showSettingsPanel = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color(0xFF1A1A1A))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A), actionIconContentColor = Color(0xFF1A1A1A))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Surface(color = Color.White, shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val canSave = (isCashSale || uiState.partyId > 0L) && uiState.items.isNotEmpty()
                            if (canSave) viewModel.saveInvoice()
                            else scope.launch { snackbarHostState.showSnackbar(if (!isCashSale && uiState.partyId <= 0L) "Select a customer first" else if (uiState.items.isEmpty()) "Add at least one item" else "Please fill all required fields") }
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary), contentPadding = PaddingValues(vertical = 14.dp)) {
                            Text("Save & New", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Button(onClick = {
                            val canSave = (isCashSale || uiState.partyId > 0L) && uiState.items.isNotEmpty()
                            if (canSave) viewModel.saveInvoice()
                            else scope.launch { snackbarHostState.showSnackbar(if (!isCashSale && uiState.partyId <= 0L) "Select a customer first" else if (uiState.items.isEmpty()) "Add at least one item" else "Please fill all required fields") }
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            enabled = !uiState.isSaving, contentPadding = PaddingValues(vertical = 14.dp)) {
                            Text(if (uiState.isSaving) "Saving..." else "Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        IconButton(onClick = { /* More options */ }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = TextPrimary)
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF5F6F6))) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Invoice No.", fontSize = 12.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(salePrefix.ifBlank { "a" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(invoiceNumberPart, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Date", fontSize = 12.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(invoiceDate, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
                        if (!partySelected) {
                            OutlinedTextField(value = customerSearchQuery, onValueChange = {
                                customerSearchQuery = it; partyName = it; partySelected = false
                                if (it.isEmpty()) viewModel.setParty(0L, "", "")
                            }, modifier = Modifier.fillMaxWidth(), label = { Text("Customer *") }, placeholder = { Text("Type name or phone number") },
                                shape = RoundedCornerShape(12.dp), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0)))
                            Spacer(modifier = Modifier.height(8.dp))
                            if (filteredParties.isNotEmpty()) {
                                filteredParties.take(5).forEach { party ->
                                    Row(modifier = Modifier.fillMaxWidth().clickable {
                                        partyName = party.name; phone = party.phone ?: ""; customerSearchQuery = party.name
                                        partySelected = true; viewModel.setParty(party.id, party.name, party.phone ?: "")
                                    }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(party.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                            if (!party.phone.isNullOrBlank()) Text(party.phone, fontSize = 12.sp, color = TextSecondary)
                                        }
                                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(partyName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    if (phone.isNotBlank()) Text(phone, fontSize = 12.sp, color = TextSecondary)
                                }
                                Icon(Icons.Filled.Close, contentDescription = "Change party", modifier = Modifier.size(20.dp).clickable {
                                    partySelected = false; customerSearchQuery = ""; partyName = ""; phone = ""; viewModel.setParty(0L, "", "")
                                }, tint = TextSecondary)
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text("Phone Number") }, shape = RoundedCornerShape(12.dp), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0)))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = ewayBillNo, onValueChange = { ewayBillNo = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text("E-Way Bill No.") }, shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0)))
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.AddItemToSale.createRoute(invoiceType)) }
                        .padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+ Add Items", fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(Optional)", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                if (uiState.items.isNotEmpty()) {
                    itemsIndexed(uiState.items) { index, item ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${index + 1}  ${item.itemName}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(String.format(java.util.Locale.US, "\u20B9%,.2f", item.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${item.quantity.toInt()} ${item.unit} x \u20B9${String.format(java.util.Locale.US, "%.2f", item.price)} = \u20B9${String.format(java.util.Locale.US, "%.2f", item.taxableAmount)}", fontSize = 12.sp, color = TextSecondary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = { viewModel.removeItem(index) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = RedAccent, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Amount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                if (uiState.totalAmount > 0) {
                                    Text(String.format(java.util.Locale.US, "\u20B9%,.2f", uiState.totalAmount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                                } else {
                                    Text("\u20B9 ------------------", fontSize = 16.sp, color = Color(0xFFBDBDBD))
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        AnimatedVisibility(visible = showSettingsPanel, enter = slideInHorizontally(initialOffsetX = { it }), exit = slideOutHorizontally(targetOffsetX = { it })) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.White, shadowElevation = 16.dp) {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) },
                            navigationIcon = { IconButton(onClick = { showSettingsPanel = false }) { Icon(Icons.Filled.Close, contentDescription = "Close") } },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary))
                    },
                    bottomBar = {
                        Surface(color = Color.White, shadowElevation = 8.dp) {
                            Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                                TextButton(onClick = { showSettingsPanel = false; navController.navigate(Screen.TransactionSettings.route) }) {
                                    Icon(Icons.Filled.Settings, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("More Settings", color = Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                ) { padding ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Sale Prefix", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    if (salePrefixEnabled) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(salePrefix, fontSize = 14.sp, color = TextSecondary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("edit", fontSize = 13.sp, color = Primary, modifier = Modifier.clickable { /* edit prefix */ })
                                        }
                                    }
                                }
                                Switch(checked = salePrefixEnabled, onCheckedChange = { salePrefixEnabled = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Transaction SMS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Send an automatic SMS to your Customer with details of the Sale.", fontSize = 13.sp, color = TextSecondary)
                                }
                                Switch(checked = transactionSms, onCheckedChange = { transactionSms = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth().clickable { /* Additional Fields */ }.padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Additional Fields", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Add extra fields to the Sale, like License Number, PAN Number, Additional Dates etc.", fontSize = 13.sp, color = TextSecondary)
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                            }
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth().clickable { /* Additional Charges */ }.padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Additional Charges", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(modifier = Modifier.size(6.dp).background(RedAccent, RoundedCornerShape(3.dp)))
                                    }
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                            }
                        }

                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                                Text("Billing Type", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { billingType = "full_sale" }) {
                                    RadioButton(selected = billingType == "full_sale", onClick = { billingType = "full_sale" }, colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                    Text("Full Sale", fontSize = 14.sp, color = TextPrimary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { billingType = "mobile_pos" }) {
                                    RadioButton(selected = billingType == "mobile_pos", onClick = { billingType = "mobile_pos" }, colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                    Text("Mobile POS", fontSize = 14.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun readPhoneContacts(context: android.content.Context): List<PhoneContact> {
    val contacts = mutableListOf<PhoneContact>()
    try {
        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
            null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: continue
                val number = it.getString(phoneIdx) ?: continue
                contacts.add(PhoneContact(name, number))
            }
        }
    } catch (_: Exception) {}
    return contacts
}

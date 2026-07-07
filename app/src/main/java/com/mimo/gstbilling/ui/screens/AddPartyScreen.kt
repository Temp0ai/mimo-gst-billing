package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.PartyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartyScreen(navController: NavController, viewModel: PartyViewModel = hiltViewModel()) {
    var partyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var partyType by remember { mutableStateOf("customer") }
    var balance by remember { mutableStateOf("") }
    var showTypeMenu by remember { mutableStateOf(false) }
    val types = listOf("customer" to "Customer", "supplier" to "Supplier", "both" to "Both")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add New Party", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary), border = ButtonDefaults.outlinedButtonBorder) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                Button(onClick = { if (partyName.isNotBlank() && phone.isNotBlank()) { viewModel.addParty(1L, partyName, phone.ifBlank { null }, email.ifBlank { null }, gstin.ifBlank { null }, address.ifBlank { null }, null, partyType); navController.popBackStack() } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = partyName, onValueChange = { partyName = it }, label = { Text("Party Name *", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Primary) })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number *", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = Primary) })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Primary) })
                    OutlinedTextField(value = gstin, onValueChange = { gstin = it }, label = { Text("GSTIN", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = Primary) })
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), minLines = 2, leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null, tint = Primary) })

                    ExposedDropdownMenuBox(expanded = showTypeMenu, onExpandedChange = { showTypeMenu = it }) {
                        OutlinedTextField(value = types.first { it.first == partyType }.second, onValueChange = {}, readOnly = true, label = { Text("Party Type", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(10.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) }, leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null, tint = Primary) })
                        ExposedDropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                            types.forEach { (value, label) -> DropdownMenuItem(text = { Text(label) }, onClick = { partyType = value; showTypeMenu = false }) }
                        }
                    }

                    OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Opening Balance", fontSize = 14.sp) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = null, tint = Primary) })
                }
            }
        }
    }
}

package com.mimo.gstbilling.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.CaAccessEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.CaShareViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaShareScreen(navController: NavController, viewModel: CaShareViewModel = hiltViewModel()) {
    val caList by viewModel.caList.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<CaAccessEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<CaAccessEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    LaunchedEffect(Unit) {
        viewModel.shareResult.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    if (showAddDialog) {
        AddEditCaDialog(
            ca = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, email, phone, gstin, firm, level ->
                viewModel.addCa(name, email, phone, gstin, firm, level)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { ca ->
        AddEditCaDialog(
            ca = ca,
            onDismiss = { showEditDialog = null },
            onSave = { name, email, phone, gstin, firm, level ->
                viewModel.updateCa(ca.copy(caName = name, caEmail = email, caPhone = phone, caGstin = gstin, firmName = firm, accessLevel = level))
                showEditDialog = null
            }
        )
    }

    showDeleteDialog?.let { ca ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Remove CA?", fontWeight = FontWeight.Bold) },
            text = { Text("Remove ${ca.caName} from your CA list?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteCa(ca); showDeleteDialog = null }) { Text("Remove", color = RedAccent, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share with CA", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = VyaparBlue, contentColor = Color.White) {
                Icon(Icons.Filled.Add, contentDescription = "Add CA")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (caList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No CA added yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add your Chartered Accountant to share data", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                item {
                    Text("${caList.size} CA(s) added", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                }
                items(caList, key = { it.id }) { ca ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).background(VyaparBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ca.caName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text(ca.caEmail, fontSize = 12.sp, color = TextSecondary)
                                    if (!ca.firmName.isNullOrBlank()) Text(ca.firmName, fontSize = 12.sp, color = VyaparBlue)
                                }
                                Box(modifier = Modifier.background(
                                    when (ca.accessLevel) { "full_access" -> VyaparGreen.copy(alpha = 0.1f); "download" -> VyaparBlue.copy(alpha = 0.1f); else -> TextSecondary.copy(alpha = 0.1f) },
                                    RoundedCornerShape(50)
                                ).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(ca.accessLevel.replace("_", " ").uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = when (ca.accessLevel) { "full_access" -> VyaparGreen; "download" -> VyaparBlue; else -> TextSecondary })
                                }
                            }

                            if (ca.lastSharedAt > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Last shared: ${dateFormat.format(Date(ca.lastSharedAt))}", fontSize = 11.sp, color = TextSecondary)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = VyaparDivider)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { viewModel.shareGstr1WithCa(ca) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(50), colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparBlue)) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GSTR-1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(onClick = { viewModel.shareAllDataWithCa(ca) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(50), colors = ButtonDefaults.outlinedButtonColors(contentColor = VyaparGreen)) {
                                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("All Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { showEditDialog = ca }, modifier = Modifier.weight(1f)) { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Edit", fontSize = 12.sp) }
                                TextButton(onClick = { showDeleteDialog = ca }, modifier = Modifier.weight(1f), colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) { Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Remove", fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCaDialog(ca: CaAccessEntity?, onDismiss: () -> Unit, onSave: (String, String, String?, String?, String?, String) -> Unit) {
    var caName by remember { mutableStateOf(ca?.caName ?: "") }
    var caEmail by remember { mutableStateOf(ca?.caEmail ?: "") }
    var caPhone by remember { mutableStateOf(ca?.caPhone ?: "") }
    var caGstin by remember { mutableStateOf(ca?.caGstin ?: "") }
    var firmName by remember { mutableStateOf(ca?.firmName ?: "") }
    var accessLevel by remember { mutableStateOf(ca?.accessLevel ?: "view_only") }
    var showLevelMenu by remember { mutableStateOf(false) }
    val isEditing = ca != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit CA" else "Add CA", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = caName, onValueChange = { caName = it }, label = { Text("CA Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = VyaparBlue) })
                OutlinedTextField(value = caEmail, onValueChange = { caEmail = it }, label = { Text("Email *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = VyaparBlue) })
                OutlinedTextField(value = caPhone, onValueChange = { caPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = VyaparBlue) })
                OutlinedTextField(value = firmName, onValueChange = { firmName = it }, label = { Text("Firm Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = VyaparBlue) })
                OutlinedTextField(value = caGstin, onValueChange = { caGstin = it }, label = { Text("GSTIN") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null, tint = VyaparBlue) })

                ExposedDropdownMenuBox(expanded = showLevelMenu, onExpandedChange = { showLevelMenu = it }) {
                    OutlinedTextField(value = accessLevel.replace("_", " ").uppercase(), onValueChange = {}, readOnly = true, label = { Text("Access Level") }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLevelMenu) })
                    ExposedDropdownMenu(expanded = showLevelMenu, onDismissRequest = { showLevelMenu = false }) {
                        listOf("view_only" to "View Only", "download" to "Download", "full_access" to "Full Access").forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { accessLevel = value; showLevelMenu = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (caName.isNotBlank() && caEmail.isNotBlank()) onSave(caName, caEmail, caPhone.ifBlank { null }, caGstin.ifBlank { null }, firmName.ifBlank { null }, accessLevel) }, enabled = caName.isNotBlank() && caEmail.isNotBlank()) {
                Text(if (isEditing) "Update" else "Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

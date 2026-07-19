package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.StaffEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.StaffViewModel

val staffRoles = listOf("Owner", "Manager", "Accountant", "Staff", "Delivery Boy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(navController: NavController, viewModel: StaffViewModel = hiltViewModel()) {
    val staffList by viewModel.staff.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingStaff by remember { mutableStateOf<StaffEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<StaffEntity?>(null) }

    if (showDeleteConfirm != null) {
        AlertDialog(onDismissRequest = { showDeleteConfirm = null }, title = { Text("Delete Staff", fontWeight = FontWeight.Bold) }, text = { Text("Remove ${showDeleteConfirm!!.name} from staff?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteStaff(showDeleteConfirm!!); showDeleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } })
    }

    if (showAddEditDialog) {
        AddEditStaffDialog(staff = editingStaff, onDismiss = { showAddEditDialog = false; editingStaff = null }, onSave = { name, phone, email, role ->
            if (editingStaff != null) viewModel.editStaff(editingStaff!!.copy(name = name, phone = phone, email = email, role = role))
            else viewModel.addStaff(name, phone, email, role)
            showAddEditDialog = false; editingStaff = null
        })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Staff Management", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) },
        floatingActionButton = { FloatingActionButton(onClick = { editingStaff = null; showAddEditDialog = true }, containerColor = Primary, contentColor = Color.White) { Icon(Icons.Filled.Add, contentDescription = "Add Staff") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            item { Text("Staff Members (${staffList.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) }
            items(staffList, key = { it.id }) { member ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { editingStaff = member; showAddEditDialog = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text(member.role, fontSize = 12.sp, color = Primary)
                            member.phone?.let { Text(it, fontSize = 11.sp, color = TextSecondary) }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Switch(checked = member.isActive, onCheckedChange = { viewModel.toggleActive(member) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenBalance))
                            IconButton(onClick = { showDeleteConfirm = member }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            item { if (staffList.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.People, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("No staff members yet", fontSize = 14.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStaffDialog(staff: StaffEntity?, onDismiss: () -> Unit, onSave: (String, String?, String?, String) -> Unit) {
    var name by remember { mutableStateOf(staff?.name ?: "") }
    var phone by remember { mutableStateOf(staff?.phone ?: "") }
    var email by remember { mutableStateOf(staff?.email ?: "") }
    var role by remember { mutableStateOf(staff?.role ?: "Staff") }
    var showRoleDropdown by remember { mutableStateOf(false) }
    val isEditing = staff != null

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (isEditing) "Edit Staff" else "Add Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Box {
                    OutlinedTextField(value = role, onValueChange = {}, readOnly = true, label = { Text("Role") }, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { showRoleDropdown = true }) }, modifier = Modifier.fillMaxWidth())
                    DropdownMenu(expanded = showRoleDropdown, onDismissRequest = { showRoleDropdown = false }) { staffRoles.forEach { r -> DropdownMenuItem(text = { Text(r) }, onClick = { role = r; showRoleDropdown = false }) } }
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name, phone.ifBlank { null }, email.ifBlank { null }, role) }, enabled = name.isNotBlank()) { Text(if (isEditing) "Update" else "Add", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

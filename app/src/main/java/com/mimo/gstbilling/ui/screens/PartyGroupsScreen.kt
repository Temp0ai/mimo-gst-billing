package com.mimo.gstbilling.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.entity.PartyGroupEntity
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.PartyGroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyGroupsScreen(navController: NavController, viewModel: PartyGroupViewModel = hiltViewModel()) {
    val groups by viewModel.groups.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<PartyGroupEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<PartyGroupEntity?>(null) }

    if (showDeleteConfirm != null) {
        AlertDialog(onDismissRequest = { showDeleteConfirm = null }, title = { Text("Delete Group", fontWeight = FontWeight.Bold) }, text = { Text("Delete group '${showDeleteConfirm!!.name}'?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteGroup(showDeleteConfirm!!); showDeleteConfirm = null }, colors = ButtonDefaults.textButtonColors(contentColor = RedAccent)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") } })
    }

    if (showAddEditDialog) {
        var name by remember { mutableStateOf(editingGroup?.name ?: "") }
        var description by remember { mutableStateOf(editingGroup?.description ?: "") }
        val isEditing = editingGroup != null
        AlertDialog(onDismissRequest = { showAddEditDialog = false; editingGroup = null },
            title = { Text(if (isEditing) "Edit Group" else "Add Group", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Group Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                }
            },
            confirmButton = { TextButton(onClick = {
                if (name.isNotBlank()) {
                    if (isEditing) viewModel.editGroup(editingGroup!!.copy(name = name, description = description.ifBlank { null }))
                    else viewModel.addGroup(name, description.ifBlank { null })
                    showAddEditDialog = false; editingGroup = null
                }
            }, enabled = name.isNotBlank()) { Text(if (isEditing) "Update" else "Add", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showAddEditDialog = false; editingGroup = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Party Groups", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) },
        floatingActionButton = { FloatingActionButton(onClick = { editingGroup = null; showAddEditDialog = true }, containerColor = Primary, contentColor = Color.White) { Icon(Icons.Filled.Add, contentDescription = "Add Group") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(Background)) {
            items(groups, key = { it.id }) { group ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { editingGroup = group; showAddEditDialog = true }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))) {
                                Icon(Icons.Filled.Folder, contentDescription = null, tint = Primary, modifier = Modifier.padding(10.dp).size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(group.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            group.description?.let { Text(it, fontSize = 12.sp, color = TextSecondary) }
                        }
                        IconButton(onClick = { showDeleteConfirm = group }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            item { if (groups.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("No groups yet", fontSize = 14.sp, color = TextSecondary) } } } }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

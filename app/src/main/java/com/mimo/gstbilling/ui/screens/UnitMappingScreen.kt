package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitMappingScreen(
    navController: NavController
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var unit1 by remember { mutableStateOf("") }
    var unit2 by remember { mutableStateOf("") }
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    val units = listOf("NOS", "KGS", "MTR", "LTR", "PCS", "BOX", "BAG", "SET", "PAC", "ROL")

    val mappings = remember {
        mutableStateListOf(
            Triple("BOX", "NOS", "12"),
            Triple("KGS", "GMS", "1000"),
            Triple("LTR", "ML", "1000"),
            Triple("MTR", "CM", "100")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Mapping", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Mapping", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(expanded = expanded1, onExpandedChange = { expanded1 = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = unit1, onValueChange = {}, readOnly = true,
                                label = { Text("Unit 1") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                            )
                            ExposedDropdownMenu(expanded = expanded1, onDismissRequest = { expanded1 = false }) {
                                units.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit1 = u; expanded1 = false }) }
                            }
                        }
                        ExposedDropdownMenuBox(expanded = expanded2, onExpandedChange = { expanded2 = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = unit2, onValueChange = {}, readOnly = true,
                                label = { Text("Unit 2") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Divider)
                            )
                            ExposedDropdownMenu(expanded = expanded2, onDismissRequest = { expanded2 = false }) {
                                units.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit2 = u; expanded2 = false }) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = LightBlueBg)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("1 $unit1", fontWeight = FontWeight.SemiBold, color = Primary, fontSize = 16.sp)
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Primary, modifier = Modifier.padding(horizontal = 8.dp))
                            Text("= ? $unit2", fontWeight = FontWeight.SemiBold, color = Primary, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (unit1.isNotBlank() && unit2.isNotBlank()) {
                                    mappings.add(Triple(unit1, unit2, "1"))
                                    unit1 = ""; unit2 = ""
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null); Spacer(modifier = Modifier.width(6.dp)); Text("Add Mapping")
                        }
                        OutlinedButton(
                            onClick = { scope.launch { snackbarHostState.showSnackbar("Unit mappings saved") } },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null); Spacer(modifier = Modifier.width(6.dp)); Text("Save")
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Existing Mappings (${mappings.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    mappings.forEachIndexed { index, (from, to, ratio) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp).background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("1 $from = $ratio $to", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                Text("Conversion Ratio: $ratio", fontSize = 12.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { mappings.removeAt(index) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (index < mappings.lastIndex) HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

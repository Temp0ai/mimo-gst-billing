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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.ui.viewmodel.CompanySwitchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySwitchScreen(
    navController: NavController,
    viewModel: CompanySwitchViewModel = hiltViewModel()
) {
    val companies by viewModel.companies.collectAsState()
    val selectedCompanyId by viewModel.selectedCompanyId.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newCompanyName by remember { mutableStateOf("") }
    var newCompanyGstin by remember { mutableStateOf("") }
    var newCompanyState by remember { mutableStateOf("") }
    var newCompanyPhone by remember { mutableStateOf("") }
    var newCompanyEmail by remember { mutableStateOf("") }
    var newCompanyAddress by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Switch Company", fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("Add Company", color = VyaparBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            navController.navigate(Screen.BusinessProfile.route)
                        }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(VyaparBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Store, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Manage Companies", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium, fontFamily = FontFamily.SansSerif)
                            Text("${companies.size} companies", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.SansSerif)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }

            items(companies) { company ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            viewModel.switchCompany(company.id)
                            navController.popBackStack()
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (company.id == selectedCompanyId) VyaparBlue.copy(alpha = 0.06f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (company.id == selectedCompanyId) VyaparBlue else Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = company.name.take(1).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                company.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                fontFamily = FontFamily.SansSerif
                            )
                            if (!company.gstin.isNullOrBlank()) {
                                Text("GSTIN: ${company.gstin}", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.SansSerif)
                            }
                            if (!company.state.isNullOrBlank()) {
                                Text(company.state, fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.SansSerif)
                            }
                        }
                        if (company.id == selectedCompanyId) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = VyaparBlue, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Company", fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCompanyName,
                        onValueChange = { newCompanyName = it },
                        label = { Text("Company Name *", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif)
                    )
                    OutlinedTextField(
                        value = newCompanyGstin,
                        onValueChange = { newCompanyGstin = it },
                        label = { Text("GSTIN", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif)
                    )
                    OutlinedTextField(
                        value = newCompanyState,
                        onValueChange = { newCompanyState = it },
                        label = { Text("State", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif)
                    )
                    OutlinedTextField(
                        value = newCompanyPhone,
                        onValueChange = { newCompanyPhone = it },
                        label = { Text("Phone", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif)
                    )
                    OutlinedTextField(
                        value = newCompanyEmail,
                        onValueChange = { newCompanyEmail = it },
                        label = { Text("Email", fontFamily = FontFamily.SansSerif) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif)
                    )
                    OutlinedTextField(
                        value = newCompanyAddress,
                        onValueChange = { newCompanyAddress = it },
                        label = { Text("Address", fontFamily = FontFamily.SansSerif) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCompanyName.isNotBlank()) {
                            viewModel.addCompany(newCompanyName, newCompanyGstin.ifBlank { null })
                            newCompanyName = ""
                            newCompanyGstin = ""
                            newCompanyState = ""
                            newCompanyPhone = ""
                            newCompanyEmail = ""
                            newCompanyAddress = ""
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                ) {
                    Text("Add Company", color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary, fontFamily = FontFamily.SansSerif)
                }
            }
        )
    }
}

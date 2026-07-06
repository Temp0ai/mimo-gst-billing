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
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.dao.CompanyDao
import com.mimo.gstbilling.data.local.entity.CompanyEntity
import com.mimo.gstbilling.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyViewModel @Inject constructor(
    private val companyDao: CompanyDao
) : ViewModel() {
    val companies = companyDao.getAllCompanies()
    val selectedCompany = companyDao.getSelectedCompany()

    suspend fun addCompany(company: CompanyEntity): Long = companyDao.insertCompany(company)
    suspend fun updateCompany(company: CompanyEntity) = companyDao.updateCompany(company)
    suspend fun deleteCompany(company: CompanyEntity) = companyDao.deleteCompany(company)
    suspend fun switchCompany(id: Long) {
        companyDao.clearSelectedCompany()
        companyDao.setSelectedCompany(id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    navController: NavController,
    viewModel: CompanyViewModel = hiltViewModel()
) {
    val companies by viewModel.companies.collectAsState(initial = emptyList())
    val selected by viewModel.selectedCompany.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var editingCompany by remember { mutableStateOf<CompanyEntity?>(null) }
    var deleteCompany by remember { mutableStateOf<CompanyEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingCompany = null; showDialog = true },
                containerColor = Primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Business", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(companies) { company ->
                val isSelected = selected?.id == company.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { viewModel.switchCompany(company.id) }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Primary.copy(alpha = 0.08f) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Business, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(company.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                company.businessType?.let {
                                    Text(it, fontSize = 12.sp, color = Primary)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = GreenBalance, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        company.gstin?.takeIf { it.isNotBlank() }?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Pin, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GSTIN: $it", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        company.address?.takeIf { it.isNotBlank() }?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(it, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = {
                                editingCompany = company
                                showDialog = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { deleteCompany = company }) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", color = RedAccent)
                            }
                        }
                    }
                }
            }

            if (companies.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Business, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Businesses Found", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to add your first business", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CompanyDialog(
            company = editingCompany,
            onDismiss = { showDialog = false },
            onSave = { company ->
                scope.launch {
                    if (company.id == 0L) viewModel.addCompany(company) else viewModel.updateCompany(company)
                    showDialog = false
                }
            }
        )
    }

    deleteCompany?.let { company ->
        AlertDialog(
            onDismissRequest = { deleteCompany = null },
            title = { Text("Delete Business") },
            text = { Text("Are you sure you want to delete '${company.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.deleteCompany(company) }
                    deleteCompany = null
                }) { Text("Delete", color = RedAccent) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCompany = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CompanyDialog(
    company: CompanyEntity?,
    onDismiss: () -> Unit,
    onSave: (CompanyEntity) -> Unit
) {
    var name by remember { mutableStateOf(company?.name ?: "") }
    var gstin by remember { mutableStateOf(company?.gstin ?: "") }
    var address by remember { mutableStateOf(company?.address ?: "") }
    var phone by remember { mutableStateOf(company?.phone ?: "") }
    var email by remember { mutableStateOf(company?.email ?: "") }
    var businessType by remember { mutableStateOf(company?.businessType ?: "Retail") }
    var state by remember { mutableStateOf(company?.state ?: "") }
    var stateCode by remember { mutableStateOf(company?.stateCode ?: "") }
    var bankName by remember { mutableStateOf(company?.bankName ?: "") }
    var bankAccountNumber by remember { mutableStateOf(company?.bankAccountNumber ?: "") }
    var bankIfsc by remember { mutableStateOf(company?.bankIfsc ?: "") }
    var bankBranch by remember { mutableStateOf(company?.bankBranch ?: "") }
    var bankUpiId by remember { mutableStateOf(company?.bankUpiId ?: "") }
    var termsAndConditions by remember { mutableStateOf(company?.termsAndConditions ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (company == null) "Add Business" else "Edit Business") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Business Name *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = gstin, onValueChange = { gstin = it }, label = { Text("GSTIN") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = stateCode, onValueChange = { stateCode = it }, label = { Text("State Code") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Text("Business Type", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Retail", "Wholesale", "Service", "Manufacturing").forEach { type ->
                            FilterChip(selected = businessType == type, onClick = { businessType = type }, label = { Text(type, fontSize = 11.sp) })
                        }
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                item { Text("Bank Details (for invoices)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
                item { OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank Name") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = bankBranch, onValueChange = { bankBranch = it }, label = { Text("Branch") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = bankAccountNumber, onValueChange = { bankAccountNumber = it }, label = { Text("Account Number") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = bankIfsc, onValueChange = { bankIfsc = it }, label = { Text("IFSC Code") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = bankUpiId, onValueChange = { bankUpiId = it }, label = { Text("UPI ID (for QR code)") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. name@upi") }) }
                item { OutlinedTextField(value = termsAndConditions, onValueChange = { termsAndConditions = it }, label = { Text("Terms & Conditions") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            CompanyEntity(
                                id = company?.id ?: 0L,
                                name = name.trim(),
                                gstin = gstin.trim().ifBlank { null },
                                address = address.trim().ifBlank { null },
                                phone = phone.trim().ifBlank { null },
                                email = email.trim().ifBlank { null },
                                businessType = businessType,
                                state = state.trim().ifBlank { null },
                                stateCode = stateCode.trim().ifBlank { null },
                                logoUri = company?.logoUri,
                                signatureUri = company?.signatureUri,
                                bankName = bankName.trim().ifBlank { null },
                                bankAccountNumber = bankAccountNumber.trim().ifBlank { null },
                                bankIfsc = bankIfsc.trim().ifBlank { null },
                                bankBranch = bankBranch.trim().ifBlank { null },
                                bankUpiId = bankUpiId.trim().ifBlank { null },
                                termsAndConditions = termsAndConditions.trim().ifBlank { null },
                                isSelected = company?.isSelected ?: true,
                                createdAt = company?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

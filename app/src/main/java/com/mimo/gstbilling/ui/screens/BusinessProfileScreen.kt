package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
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

val IndianStates = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
    "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
    "Uttar Pradesh", "Uttarakhand", "West Bengal",
    "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
)

val BusinessTypes = listOf("Manufacturing", "Trading", "Service", "Retail", "Wholesale")

val BusinessCategories = listOf(
    "Accounting & CA", "Interior Designer", "Automobiles/Auto parts", "Salon & Spa",
    "Liquor Store", "Book/Stationary store", "Construction Materials & Equipment",
    "Repairing/Plumbing/Electrician", "Chemicals & Fertilizers",
    "Computer Equipments & Softwares", "Electrical & Electronics Equipments",
    "Fashion Accessory/Cosmetics", "Tailoring/Boutique", "Fruit And Vegetable",
    "Petroleum Bulk Stations & Terminals/Petrol", "Restaurant/Hotel", "Footwear",
    "Paper & Paper Products", "Sweet Shop/Bakery", "Gifts & Toys",
    "Laundry/Washing/Dry clean", "Coaching & Training", "Renting & Leasing",
    "Fitness Center", "Oil & Gas", "Real Estate", "NGO & Charitable trust",
    "Tours & Travels", "FMCG Products", "Medical/Pharmacy", "Hardware & Paints",
    "Agriculture", "Packers & Movers", "IT & IT Services",
    "Photography & Videography", "Jewellery", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    navController: NavController,
    viewModel: CompanyViewModel = hiltViewModel()
) {
    val selected by viewModel.selectedCompany.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showEditScreen by remember { mutableStateOf(false) }
    var editingCompany by remember { mutableStateOf<CompanyEntity?>(null) }
    var deleteCompany by remember { mutableStateOf<CompanyEntity?>(null) }

    if (showEditScreen) {
        BusinessProfileEditScreen(
            company = editingCompany,
            onBack = { showEditScreen = false },
            onSave = { company ->
                scope.launch {
                    if (company.id == 0L) viewModel.addCompany(company) else viewModel.updateCompany(company)
                    viewModel.switchCompany(company.id)
                    showEditScreen = false
                }
            }
        )
    } else {
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
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Background)
                    .verticalScroll(rememberScrollState())
            ) {
                selected?.let { company ->
                    BusinessCard(company = company)

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Button(
                            onClick = {
                                navController.navigate(Screen.BusinessCardDesigner.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Card")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileCompletionSection(company = company)

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { editingCompany = company; showEditScreen = true }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Edit Profile", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { deleteCompany = company }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(RedAccent.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = null, tint = RedAccent, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = RedAccent)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Business, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(80.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No Business Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Create your business profile to get started", fontSize = 14.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { editingCompany = null; showEditScreen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Business Profile")
                            }
                        }
                    }
                }
            }
        }
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
fun BusinessCard(company: CompanyEntity) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val logoBitmap = remember(company.logoUri) {
                        company.logoUri?.let { uriStr ->
                            try {
                                val uri = Uri.parse(uriStr)
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val bmp = BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()
                                bmp
                            } catch (_: Exception) { null }
                        }
                    }
                    if (logoBitmap != null) {
                        Image(
                            bitmap = logoBitmap.asImageBitmap(),
                            contentDescription = "Company Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            company.name.take(1).uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(company.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (company.businessType != null || company.businessType?.isNotBlank() == true) {
                        Text(
                            company.businessType ?: "",
                            fontSize = 13.sp,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Divider)

            company.gstin?.takeIf { it.isNotBlank() }?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BusinessCenter, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            company.phone?.takeIf { it.isNotBlank() }?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, fontSize = 13.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            company.email?.takeIf { it.isNotBlank() }?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, fontSize = 13.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            company.address?.takeIf { it.isNotBlank() }?.let {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun ProfileCompletionSection(company: CompanyEntity) {
    val fields = listOf(
        company.name.isNotBlank(),
        company.gstin?.isNotBlank() == true,
        company.phone?.isNotBlank() == true,
        company.email?.isNotBlank() == true,
        company.address?.isNotBlank() == true,
        company.state?.isNotBlank() == true,
        company.businessType?.isNotBlank() == true,
        company.bankName?.isNotBlank() == true,
        company.bankAccountNumber?.isNotBlank() == true,
        company.bankIfsc?.isNotBlank() == true
    )
    val filledCount = fields.count { it }
    val percentage = (filledCount.toFloat() / fields.size * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profile Completion", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (percentage >= 80) GreenBalance else Primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { filledCount.toFloat() / fields.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (percentage >= 80) GreenBalance else Primary,
                trackColor = Divider
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                when {
                    percentage == 100 -> "Your profile is complete!"
                    percentage >= 70 -> "Almost there! Add a few more details."
                    else -> "Complete your profile to enable all features."
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileEditScreen(
    company: CompanyEntity?,
    onBack: () -> Unit,
    onSave: (CompanyEntity) -> Unit
) {
    var name by remember { mutableStateOf(company?.name ?: "") }
    var gstin by remember { mutableStateOf(company?.gstin ?: "") }
    var phone by remember { mutableStateOf(company?.phone ?: "") }
    var phone2 by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(company?.email ?: "") }
    var address by remember { mutableStateOf(company?.address ?: "") }
    var pincode by remember { mutableStateOf("") }
    var businessDescription by remember { mutableStateOf("") }
    var signatureUri by remember { mutableStateOf(company?.signatureUri) }

    var state by remember { mutableStateOf(company?.state ?: "") }
    var businessType by remember { mutableStateOf(company?.businessType ?: "") }
    var businessCategory by remember { mutableStateOf("") }
    var showTypeOnCard by remember { mutableStateOf(true) }
    var showCategoryOnCard by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showStateDropdown by remember { mutableStateOf(false) }
    var showBusinessTypeDropdown by remember { mutableStateOf(false) }
    var showBusinessCategoryDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Business Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
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
                                    businessType = businessType.trim().ifBlank { null },
                                    state = state.trim().ifBlank { null },
                                    stateCode = null,
                                    logoUri = company?.logoUri,
                                    signatureUri = signatureUri,
                                    bankName = company?.bankName,
                                    bankAccountNumber = company?.bankAccountNumber,
                                    bankIfsc = company?.bankIfsc,
                                    bankBranch = company?.bankBranch,
                                    bankUpiId = company?.bankUpiId,
                                    termsAndConditions = company?.termsAndConditions,
                                    isSelected = company?.isSelected ?: true,
                                    createdAt = company?.createdAt ?: System.currentTimeMillis()
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Primary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.padding(start = tabPositions[selectedTab].left, end = tabPositions[selectedTab].right),
                            height = 3.dp,
                            color = Primary
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Basic Details",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Primary else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Business Details",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Primary else TextSecondary
                        )
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    BasicDetailsTab(
                        name = name, onNameChange = { name = it },
                        gstin = gstin, onGstinChange = { gstin = it },
                        phone = phone, onPhoneChange = { phone = it },
                        phone2 = phone2, onPhone2Change = { phone2 = it },
                        email = email, onEmailChange = { email = it },
                        address = address, onAddressChange = { address = it },
                        pincode = pincode, onPincodeChange = { pincode = it },
                        businessDescription = businessDescription, onBusinessDescriptionChange = { businessDescription = it },
                        signatureUri = signatureUri, onSignatureChange = { signatureUri = it },
                        gstinVerified = company?.gstin?.isNotBlank() == true
                    )
                } else {
                    BusinessDetailsTab(
                        state = state, onStateChange = { state = it },
                        businessType = businessType, onBusinessTypeChange = { businessType = it },
                        businessCategory = businessCategory, onBusinessCategoryChange = { businessCategory = it },
                        showTypeOnCard = showTypeOnCard, onShowTypeOnCardChange = { showTypeOnCard = it },
                        showCategoryOnCard = showCategoryOnCard, onShowCategoryOnCardChange = { showCategoryOnCard = it },
                        showStateDropdown = showStateDropdown, onShowStateDropdownChange = { showStateDropdown = it },
                        showBusinessTypeDropdown = showBusinessTypeDropdown, onShowBusinessTypeDropdownChange = { showBusinessTypeDropdown = it },
                        showBusinessCategoryDropdown = showBusinessCategoryDropdown, onShowBusinessCategoryDropdownChange = { showBusinessCategoryDropdown = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicDetailsTab(
    name: String, onNameChange: (String) -> Unit,
    gstin: String, onGstinChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    phone2: String, onPhone2Change: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    pincode: String, onPincodeChange: (String) -> Unit,
    businessDescription: String, onBusinessDescriptionChange: (String) -> Unit,
    signatureUri: String?, onSignatureChange: (String?) -> Unit,
    gstinVerified: Boolean
) {
    val context = LocalContext.current
    Text("Business Name", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = { Text("Enter business name") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text("GSTIN", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            OutlinedTextField(
                value = gstin,
                onValueChange = onGstinChange,
                placeholder = { Text("Enter GSTIN") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
        if (gstinVerified && gstin.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .background(GreenBalance.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = GreenBalance, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verified", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GreenBalance)
                }
            }
        }
    }

    Text("Phone Number 1", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        placeholder = { Text("Enter phone number") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = TextSecondary) }
    )

    Text("Phone Number 2 (Optional)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    OutlinedTextField(
        value = phone2,
        onValueChange = onPhone2Change,
        placeholder = { Text("Enter alternate phone number") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = TextSecondary) }
    )

    Text("Email ID", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = { Text("Enter email address") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = TextSecondary) }
    )

    Text("Business Address", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    OutlinedTextField(
        value = address,
        onValueChange = onAddressChange,
        placeholder = { Text("Enter full business address") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 3,
        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextSecondary) }
    )

    Text("Pincode", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    OutlinedTextField(
        value = pincode,
        onValueChange = onPincodeChange,
        placeholder = { Text("Enter pincode") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )

    Text("Business Description", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    OutlinedTextField(
        value = businessDescription,
        onValueChange = onBusinessDescriptionChange,
        placeholder = { Text("Describe your business") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = 2
    )

    Text("Signature", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sigBitmap = remember(signatureUri) {
                signatureUri?.let { uriStr ->
                    try {
                        val uri = Uri.parse(uriStr)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bmp = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        bmp
                    } catch (_: Exception) { null }
                }
            }
            if (sigBitmap != null) {
                Image(
                    bitmap = sigBitmap.asImageBitmap(),
                    contentDescription = "Signature",
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Divider.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No signature added", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Change signature */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { /* TODO: Upload signature */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", fontSize = 12.sp)
                }
                if (signatureUri != null) {
                    OutlinedButton(
                        onClick = { onSignatureChange(null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailsTab(
    state: String, onStateChange: (String) -> Unit,
    businessType: String, onBusinessTypeChange: (String) -> Unit,
    businessCategory: String, onBusinessCategoryChange: (String) -> Unit,
    showTypeOnCard: Boolean, onShowTypeOnCardChange: (Boolean) -> Unit,
    showCategoryOnCard: Boolean, onShowCategoryOnCardChange: (Boolean) -> Unit,
    showStateDropdown: Boolean, onShowStateDropdownChange: (Boolean) -> Unit,
    showBusinessTypeDropdown: Boolean, onShowBusinessTypeDropdownChange: (Boolean) -> Unit,
    showBusinessCategoryDropdown: Boolean, onShowBusinessCategoryDropdownChange: (Boolean) -> Unit
) {
    Text("State", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    ExposedDropdownMenuBox(
        expanded = showStateDropdown,
        onExpandedChange = onShowStateDropdownChange
    ) {
        OutlinedTextField(
            value = state,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select state") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStateDropdown) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = showStateDropdown,
            onDismissRequest = { onShowStateDropdownChange(false) }
        ) {
            IndianStates.forEach { stateName ->
                DropdownMenuItem(
                    text = { Text(stateName) },
                    onClick = {
                        onStateChange(stateName)
                        onShowStateDropdownChange(false)
                    }
                )
            }
        }
    }

    Text("Business Type", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    ExposedDropdownMenuBox(
        expanded = showBusinessTypeDropdown,
        onExpandedChange = onShowBusinessTypeDropdownChange
    ) {
        OutlinedTextField(
            value = businessType,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select business type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBusinessTypeDropdown) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = showBusinessTypeDropdown,
            onDismissRequest = { onShowBusinessTypeDropdownChange(false) }
        ) {
            BusinessTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onBusinessTypeChange(type)
                        onShowBusinessTypeDropdownChange(false)
                    }
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Show on card", fontSize = 13.sp, color = TextPrimary)
        Switch(
            checked = showTypeOnCard,
            onCheckedChange = onShowTypeOnCardChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary
            )
        )
    }

    HorizontalDivider(color = Divider)

    Text("Business Category", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    ExposedDropdownMenuBox(
        expanded = showBusinessCategoryDropdown,
        onExpandedChange = onShowBusinessCategoryDropdownChange
    ) {
        OutlinedTextField(
            value = businessCategory,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select business category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBusinessCategoryDropdown) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = showBusinessCategoryDropdown,
            onDismissRequest = { onShowBusinessCategoryDropdownChange(false) }
        ) {
            BusinessCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onBusinessCategoryChange(category)
                        onShowBusinessCategoryDropdownChange(false)
                    }
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Show on card", fontSize = 13.sp, color = TextPrimary)
        Switch(
            checked = showCategoryOnCard,
            onCheckedChange = onShowCategoryOnCardChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary
            )
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
}

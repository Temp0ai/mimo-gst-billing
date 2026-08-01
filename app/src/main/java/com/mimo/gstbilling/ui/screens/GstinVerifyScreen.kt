package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.utils.GstinVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstinVerifyScreen(
    onBack: () -> Unit,
    preFilledGstin: String = ""
) {
    var gstinInput by remember { mutableStateOf(preFilledGstin) }
    var result by remember { mutableStateOf<GstinVerifier.GstinResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var verifyOnline by remember { mutableStateOf(false) }
    var recentVerifications by remember { mutableStateOf(listOf<GstinVerifier.GstinResult>()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GSTIN Verify", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = VyaparBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Enter GSTIN Number", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VyaparTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = gstinInput,
                        onValueChange = { gstinInput = it.uppercase().take(15) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("GSTIN (15 characters)") },
                        placeholder = { Text("27AAPFU0939F1ZV") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii
                        ),
                        leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = VyaparBlue) },
                        trailingIcon = {
                            if (gstinInput.isNotEmpty()) {
                                IconButton(onClick = { gstinInput = ""; result = null }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = VyaparTextSecondary)
                                }
                            }
                        },
                        isError = gstinInput.isNotEmpty() && gstinInput.length != 15
                    )

                    if (gstinInput.isNotEmpty() && gstinInput.length != 15) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${gstinInput.length}/15 characters", fontSize = 12.sp, color = VyaparRed)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = verifyOnline,
                            onCheckedChange = { verifyOnline = it }
                        )
                        Text("Verify via GSTN API (online)", fontSize = 14.sp, color = VyaparTextPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    result = withContext(Dispatchers.IO) {
                                        if (verifyOnline) {
                                            GstinVerifier.verifyViaApi(gstinInput.trim())
                                        } else {
                                            GstinVerifier.validateFormat(gstinInput.trim())
                                        }
                                    }
                                    result?.let {
                                        recentVerifications = listOf(it) + recentVerifications.filter { r -> r.gstin != it.gstin }.take(9)
                                    }
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = gstinInput.length == 15 && !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify", fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    gstinInput = ""
                                    result = null
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            result?.let { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val statusColor = when {
                                res.isFullyValid && res.isLive == true -> VyaparGreen
                                res.isFullyValid && res.isLive == false -> VyaparRed
                                res.isFullyValid -> Color(0xFFFF9800)
                                else -> VyaparRed
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(statusColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when {
                                        res.isFullyValid && res.isLive == true -> Icons.Filled.CheckCircle
                                        res.isFullyValid -> Icons.Filled.Warning
                                        else -> Icons.Filled.Error
                                    },
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    res.displayStatus,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = statusColor
                                )
                                Text(res.gstin, fontSize = 13.sp, color = VyaparTextSecondary)
                            }
                        }

                        if (!res.isFullyValid) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = VyaparErrorBackground)
                            ) {
                                Text(
                                    res.errorMessage ?: "Invalid GSTIN",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 13.sp,
                                    color = VyaparRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = VyaparDivider)
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow("GSTIN", res.gstin)
                        res.stateName?.let { DetailRow("State", "${res.stateCode} - $it") }
                        res.entityType?.let { DetailRow("Entity Type", it) }
                        res.panNumber.takeIf { it.isNotEmpty() }?.let { DetailRow("PAN", it) }

                        if (verifyOnline) {
                            res.businessName?.let { DetailRow("Business Name", it) }
                            res.registrationDate?.let { DetailRow("Registration Date", it) }
                            res.status?.let { DetailRow("GST Status", it) }
                            res.isLive?.let { DetailRow("Active", if (it) "Yes" else "No") }
                        }

                        if (res.errorMessage != null && res.isFullyValid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                            ) {
                                Text(
                                    res.errorMessage!!,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }

            if (recentVerifications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent Verifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyaparTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                recentVerifications.take(5).forEach { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        onClick = {
                            gstinInput = res.gstin
                            result = res
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dotColor = when {
                                res.isFullyValid && res.isLive == true -> VyaparGreen
                                res.isFullyValid -> Color(0xFFFF9800)
                                else -> VyaparRed
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(res.gstin, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VyaparTextPrimary)
                                Text(res.displayStatus, fontSize = 12.sp, color = VyaparTextSecondary)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = VyaparTextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = VyaparTextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VyaparTextPrimary)
    }
}

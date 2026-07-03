package com.mimo.gstbilling.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.GreenBalance
import com.mimo.gstbilling.ui.theme.LightBlueBg
import com.mimo.gstbilling.ui.theme.Primary
import com.mimo.gstbilling.ui.theme.RedAccent
import com.mimo.gstbilling.ui.theme.TextPrimary
import com.mimo.gstbilling.ui.theme.TextSecondary
import com.mimo.gstbilling.utils.CsvImporter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(navController: NavController) {
    val context = LocalContext.current
    var importedCount by remember { mutableIntStateOf(0) }
    var failedCount by remember { mutableIntStateOf(0) }
    var importType by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    var showResult by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                val resolver = context.contentResolver
                val inputStream = resolver.openInputStream(it)
                val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                val lines = reader.readLines()
                reader.close()

                if (lines.size <= 1) {
                    errors = listOf("File is empty or has no data rows")
                    showResult = true
                    return@let
                }

                val header = lines[0].lowercase().split(",").map { h -> h.trim().removeSurrounding("\"") }

                if (importType.contains("item", ignoreCase = true) || importType.isEmpty()) {
                    val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("item") }
                    if (nameIdx == -1) {
                        errors = listOf("Could not find 'name' column in header: ${lines[0]}")
                        showResult = true
                        return@let
                    }
                    var success = 0
                    var failed = 0
                    val errs = mutableListOf<String>()
                    for (i in 1 until lines.size) {
                        try {
                            val cols = lines[i].split(",").map { c -> c.trim().removeSurrounding("\"") }
                            val name = cols.getOrElse(nameIdx) { "" }
                            if (name.isBlank()) { failed++; errs.add("Row ${i + 1}: empty name"); continue }
                            success++
                        } catch (e: Exception) { failed++; errs.add("Row ${i + 1}: ${e.message}") }
                    }
                    importedCount = success
                    failedCount = failed
                    errors = errs
                } else {
                    val nameIdx = header.indexOfFirst { it.contains("name") || it.contains("party") }
                    if (nameIdx == -1) {
                        errors = listOf("Could not find 'name' column in header: ${lines[0]}")
                        showResult = true
                        return@let
                    }
                    var success = 0
                    var failed = 0
                    val errs = mutableListOf<String>()
                    for (i in 1 until lines.size) {
                        try {
                            val cols = lines[i].split(",").map { c -> c.trim().removeSurrounding("\"") }
                            val name = cols.getOrElse(nameIdx) { "" }
                            if (name.isBlank()) { failed++; errs.add("Row ${i + 1}: empty name"); continue }
                            success++
                        } catch (e: Exception) { failed++; errs.add("Row ${i + 1}: ${e.message}") }
                    }
                    importedCount = success
                    failedCount = failed
                    errors = errs
                }
                showResult = true
            } catch (e: Exception) {
                errors = listOf("Error reading file: ${e.message}")
                showResult = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Import Data", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Import from CSV", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Supported: .csv files with headers", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Expected columns for Items: name, hsn, price/gst rate, unit, category", fontSize = 11.sp, color = TextSecondary)
                    Text("Expected columns for Parties: name, phone, gstin, email, address, type", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Import Items", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text("Import items with name, HSN, price, GST rate", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        importType = "items"
                        launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel"))
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Text("Select Items CSV File")
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Import Parties", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text("Import parties with name, phone, GSTIN, address", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        importType = "parties"
                        launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel"))
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Text("Select Parties CSV File")
                    }
                }
            }

            if (showResult) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (importedCount > 0) GreenBalance.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (importedCount > 0) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = if (importedCount > 0) GreenBalance else RedAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (importedCount > 0) "Imported $importedCount $importType records" else "Import failed",
                                fontWeight = FontWeight.Bold,
                                color = if (importedCount > 0) GreenBalance else RedAccent
                            )
                        }
                        if (failedCount > 0) {
                            Text("$failedCount rows failed", fontSize = 12.sp, color = RedAccent)
                        }
                        if (errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Errors:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            errors.take(5).forEach { err ->
                                Text(err, fontSize = 11.sp, color = TextSecondary)
                            }
                            if (errors.size > 5) {
                                Text("... and ${errors.size - 5} more errors", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.mimo.gstbilling.ui.screens

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.mimo.gstbilling.data.local.dao.ItemDao
import com.mimo.gstbilling.data.local.dao.PartyDao
import com.mimo.gstbilling.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val partyDao: PartyDao,
    private val itemDao: ItemDao
) : ViewModel() {
    suspend fun getPartiesCsv(companyId: Long): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("name,phone,gstin,email,address,state,type,balance")
        val parties = partyDao.getPartiesByCompany(companyId).first()
        parties.forEach { p ->
            sb.appendLine("\"${p.name}\",\"${p.phone ?: ""}\",\"${p.gstin ?: ""}\",\"${p.email ?: ""}\",\"${p.address ?: ""}\",\"${p.state ?: ""}\",\"${p.partyType}\",${p.balance}")
        }
        sb.toString()
    }

    suspend fun getItemsCsv(companyId: Long): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("name,hsn,sale_price,purchase_price,gst_rate,unit,stock,is_service")
        val items = itemDao.getItemsByCompany(companyId).first()
        items.forEach { i ->
            sb.appendLine("\"${i.name}\",\"${i.hsnCode ?: ""}\",${i.salePrice},${i.purchasePrice},${i.gstRate},\"${i.unit}\",${i.stockQuantity},${i.isService}")
        }
        sb.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataScreen(
    navController: NavController,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exported by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf("") }

    fun saveCsv(content: String, filename: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        os.write(content.toByteArray())
                    }
                }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(dir, filename)
                file.writeText(content)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBlueBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null, tint = Primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Export to CSV", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Export data to CSV files in Downloads folder", fontSize = 13.sp, color = TextSecondary)
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.People, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Export Parties", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("All customers and suppliers", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val csv = viewModel.getPartiesCsv(1L)
                                val saved = saveCsv(csv, "mimo_parties_export.csv")
                                exported = saved
                                exportType = "parties"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Export Parties CSV") }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, tint = Color(0xFF00BCD4), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Export Items", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("All products and services", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val csv = viewModel.getItemsCsv(1L)
                                val saved = saveCsv(csv, "mimo_items_export.csv")
                                exported = saved
                                exportType = "items"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
                    ) { Text("Export Items CSV") }
                }
            }

            if (exported) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GreenBalance.copy(alpha = 0.1f))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenBalance, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exported to Downloads/mimo_${exportType}_export.csv", fontWeight = FontWeight.Bold, color = GreenBalance)
                    }
                }
            }
        }
    }
}

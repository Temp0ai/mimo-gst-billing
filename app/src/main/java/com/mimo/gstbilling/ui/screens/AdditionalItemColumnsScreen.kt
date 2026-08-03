package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import org.json.JSONArray

data class ItemColumn(val id: String, val name: String, val enabled: Boolean)

private val DEFAULT_COLUMNS = listOf(
    ItemColumn("hsn", "HSN/SAC Code", true),
    ItemColumn("sku", "SKU", true),
    ItemColumn("barcode", "Barcode", false),
    ItemColumn("batch", "Batch Number", false),
    ItemColumn("expiry", "Expiry Date", false),
    ItemColumn("serial", "Serial Number", false),
    ItemColumn("discount", "Discount %", true),
    ItemColumn("tax", "Tax Rate", true),
    ItemColumn("unit", "Unit", true),
    ItemColumn("description", "Description", false),
    ItemColumn("category", "Category", false),
    ItemColumn("location", "Storage Location", false),
    ItemColumn("reorderLevel", "Reorder Level", false),
    ItemColumn("purchasePrice", "Purchase Price", false),
    ItemColumn("margin", "Margin %", false)
)

private fun saveColumns(context: Context, columns: List<ItemColumn>) {
    val arr = JSONArray()
    columns.forEach { col ->
        val obj = org.json.JSONObject()
        obj.put("id", col.id)
        obj.put("name", col.name)
        obj.put("enabled", col.enabled)
        arr.put(obj)
    }
    context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE)
        .edit().putString("item_columns", arr.toString()).apply()
}

private fun loadColumns(context: Context): List<ItemColumn> {
    val json = context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE).getString("item_columns", null)
    if (json == null) { saveColumns(context, DEFAULT_COLUMNS); return DEFAULT_COLUMNS }
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            ItemColumn(obj.getString("id"), obj.getString("name"), obj.getBoolean("enabled"))
        }
    } catch (_: Exception) { DEFAULT_COLUMNS }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalItemColumnsScreen(navController: NavController) {
    val context = LocalContext.current
    var columns by remember { mutableStateOf(loadColumns(context)) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Item Columns", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = VyaparBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Toggle columns visible in item list and invoice line items", fontSize = 13.sp, color = VyaparTextSecondary)
                }
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(columns) { column ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(column.name, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Switch(checked = column.enabled, onCheckedChange = { enabled -> columns = columns.map { if (it.id == column.id) it.copy(enabled = enabled) else it } })
                        }
                    }
                }
            }
            Button(onClick = { saveColumns(context, columns); navController.popBackStack() }, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                Text("Save Columns", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import org.json.JSONObject

data class DeletedItem(val id: Long, val name: String, val type: String, val amount: Double, val deletedAt: Long)

fun getDeletedItems(context: Context): MutableList<DeletedItem> {
    val prefs = context.getSharedPreferences("recycle_bin", Context.MODE_PRIVATE)
    val json = prefs.getString("deleted_items", "[]") ?: "[]"
    val arr = JSONArray(json)
    val items = mutableListOf<DeletedItem>()
    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        items.add(DeletedItem(obj.getLong("id"), obj.getString("name"), obj.getString("type"), obj.getDouble("amount"), obj.getLong("deletedAt")))
    }
    return items
}

fun saveDeletedItems(context: Context, items: List<DeletedItem>) {
    val arr = JSONArray()
    items.forEach { item ->
        arr.put(JSONObject().apply {
            put("id", item.id); put("name", item.name); put("type", item.type)
            put("amount", item.amount); put("deletedAt", item.deletedAt)
        })
    }
    context.getSharedPreferences("recycle_bin", Context.MODE_PRIVATE).edit().putString("deleted_items", arr.toString()).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(navController: NavController) {
    val context = LocalContext.current
    var deletedItems by remember { mutableStateOf(getDeletedItems(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recycle Bin", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary)
            )
        }
    ) { padding ->
        if (deletedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recycle Bin is empty", fontSize = 16.sp, color = TextSecondary)
                    Text("Deleted items will appear here", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${deletedItems.size} items", fontSize = 14.sp, color = TextSecondary)
                        TextButton(onClick = { deletedItems = mutableListOf(); saveDeletedItems(context, emptyList()) }) {
                            Text("Empty All", color = RedAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(deletedItems.size) { index ->
                    val item = deletedItems[index]
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(
                                when (item.type) { "Invoice" -> Primary.copy(alpha = 0.1f); "Party" -> GreenBalance.copy(alpha = 0.1f); else -> RedAccent.copy(alpha = 0.1f) },
                                RoundedCornerShape(8.dp)
                            ), contentAlignment = Alignment.Center) {
                                Icon(
                                    when (item.type) { "Invoice" -> Icons.Filled.Receipt; "Party" -> Icons.Filled.Business; else -> Icons.Filled.Inventory },
                                    contentDescription = null,
                                    tint = when (item.type) { "Invoice" -> Primary; "Party" -> GreenBalance; else -> RedAccent },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("${item.type}", fontSize = 12.sp, color = TextSecondary)
                            }
                            if (item.amount > 0) {
                                Text(String.format(java.util.Locale.US, "\u20B9%,.0f", item.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            IconButton(onClick = {
                                deletedItems = deletedItems.toMutableList().also { it.removeAt(index) }
                                saveDeletedItems(context, deletedItems)
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Restore, contentDescription = "Restore", tint = GreenBalance, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                deletedItems = deletedItems.toMutableList().also { it.removeAt(index) }
                                saveDeletedItems(context, deletedItems)
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = "Permanently Delete", tint = RedAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

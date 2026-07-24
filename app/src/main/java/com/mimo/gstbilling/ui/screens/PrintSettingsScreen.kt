package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RemoveRedEye
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
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("print_settings", Context.MODE_PRIVATE)
    var printFormat by remember { mutableStateOf(prefs.getString("format", "A4") ?: "A4") }
    var showCopies by remember { mutableStateOf(prefs.getInt("copies", 1)) }
    var showFormatDropdown by remember { mutableStateOf(false) }
    var paperSize by remember { mutableStateOf(prefs.getString("paper_size", "A4") ?: "A4") }
    var showPaperDropdown by remember { mutableStateOf(false) }
    var orientation by remember { mutableStateOf(prefs.getString("orientation", "Portrait") ?: "Portrait") }
    var showOrientationDropdown by remember { mutableStateOf(false) }

    fun save(key: String, value: Any) {
        when (value) {
            is String -> prefs.edit().putString(key, value).apply()
            is Int -> prefs.edit().putInt(key, value).apply()
            is Boolean -> prefs.edit().putBoolean(key, value).apply()
        }
    }

    val formats = listOf("A4", "A5", "Thermal 80mm", "Thermal 58mm")
    val paperSizes = listOf("A4", "A5", "Letter", "Legal")
    val orientations = listOf("Portrait", "Landscape")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Print Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VyaparBackground)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Format Selection Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Print Format", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                    // Format dropdown
                    Box {
                        OutlinedTextField(
                            value = printFormat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Invoice Format") },
                            trailingIcon = {
                                Text("\u25BC", modifier = Modifier.clickable { showFormatDropdown = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = showFormatDropdown, onDismissRequest = { showFormatDropdown = false }) {
                            formats.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f) },
                                    onClick = {
                                        printFormat = f
                                        save("format", f)
                                        showFormatDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Paper size dropdown
                    Box {
                        OutlinedTextField(
                            value = paperSize,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Paper Size") },
                            trailingIcon = {
                                Text("\u25BC", modifier = Modifier.clickable { showPaperDropdown = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = showPaperDropdown, onDismissRequest = { showPaperDropdown = false }) {
                            paperSizes.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        paperSize = s
                                        save("paper_size", s)
                                        showPaperDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Orientation dropdown
                    Box {
                        OutlinedTextField(
                            value = orientation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Orientation") },
                            trailingIcon = {
                                Text("\u25BC", modifier = Modifier.clickable { showOrientationDropdown = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = showOrientationDropdown, onDismissRequest = { showOrientationDropdown = false }) {
                            orientations.forEach { o ->
                                DropdownMenuItem(
                                    text = { Text(o) },
                                    onClick = {
                                        orientation = o
                                        save("orientation", o)
                                        showOrientationDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Default copies
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Default Copies", fontSize = 14.sp, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                if (showCopies > 1) {
                                    showCopies--
                                    save("copies", showCopies)
                                }
                            }) {
                                Text("-", fontSize = 20.sp, color = Primary, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                showCopies.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            TextButton(onClick = {
                                showCopies++
                                save("copies", showCopies)
                            }) {
                                Text("+", fontSize = 20.sp, color = Primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview Button
            Button(
                onClick = {
                    navController.navigate(Screen.InvoicePreview.createRoute(0L))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VyaparBlue)
            ) {
                Icon(Icons.Filled.RemoveRedEye, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preview Invoice", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Print Button
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print Test Page", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSwitcherScreen(navController: NavController) {
    var selectedTheme by remember { mutableStateOf("light") }
    val themes = listOf(
        Triple("light", "Light", Color.White),
        Triple("dark", "Dark", Color(0xFF121212)),
        Triple("blue", "Ocean Blue", Color(0xFF1565C0)),
        Triple("green", "Forest Green", Color(0xFF2E7D32)),
        Triple("purple", "Royal Purple", Color(0xFF6A1B9A)),
        Triple("sunset", "Sunset Orange", Color(0xFFE65100))
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Theme", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = TextPrimary))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(LightBlueBg).padding(16.dp)) {
            Text("Choose Theme", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(themes) { (id, name, color) ->
                    Card(modifier = Modifier.border(if (selectedTheme == id) 3.dp else 0.dp, if (selectedTheme == id) RedAccent else Color.Transparent, RoundedCornerShape(16.dp)).clickable { selectedTheme = id }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(60.dp).background(color, RoundedCornerShape(12.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            if (selectedTheme == id) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                Text("Apply Theme", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

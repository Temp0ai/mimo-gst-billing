package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    var userName by remember { mutableStateOf(prefs.getString("name", "Business Owner") ?: "Business Owner") }
    var userEmail by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var userPhone by remember { mutableStateOf(prefs.getString("phone", "") ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Profile", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }, actions = { TextButton(onClick = { isEditing = !isEditing }) { Text(if (isEditing) "Save" else "Edit", color = Primary, fontWeight = FontWeight.Bold) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color(0xFF1A1A1A), navigationIconContentColor = Color(0xFF1A1A1A))) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Background).navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) { Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)), modifier = Modifier.size(80.dp)) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(userName.take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Primary) } } }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = userName, onValueChange = { userName = it }, label = { Text("Name") }, readOnly = !isEditing, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = userEmail, onValueChange = { userEmail = it }, label = { Text("Email") }, readOnly = !isEditing, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = userPhone, onValueChange = { userPhone = it }, label = { Text("Phone") }, readOnly = !isEditing, modifier = Modifier.fillMaxWidth())
                }
            }
            if (isEditing) {
                Button(onClick = { prefs.edit().putString("name", userName).putString("email", userEmail).putString("phone", userPhone).apply(); isEditing = false }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) { Text("Save Profile", fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Account Info", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Role", fontSize = 14.sp, color = TextSecondary); Text("Owner", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Plan", fontSize = 14.sp, color = TextSecondary); Text("Free", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenBalance) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("App Version", fontSize = 14.sp, color = TextSecondary); Text("v148", fontSize = 14.sp, color = TextPrimary) }
                }
            }
        }
    }
}

package com.mimo.gstbilling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*

@Composable
fun LoginScreen(navController: NavController, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mimo_prefs", Context.MODE_PRIVATE) }
    val isLoggedIn = remember { prefs.getBoolean("logged_in", false) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onLoginSuccess()
    }

    if (isLoggedIn) return

    Box(modifier = Modifier.fillMaxSize().background(Primary), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth().padding(32.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Business, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Mimo GST Billing", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text("Log in to continue", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; error = "" },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = "" },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = RedAccent, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            error = "Please enter username and password"
                        } else if (username == "admin" && password == "admin") {
                            prefs.edit().putBoolean("logged_in", true).putString("username", username).apply()
                            onLoginSuccess()
                        } else {
                            error = "Invalid credentials"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                ) {
                    Text("Log In", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Default: admin / admin", fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

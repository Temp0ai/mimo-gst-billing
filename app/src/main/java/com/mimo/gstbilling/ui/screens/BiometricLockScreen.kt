package com.mimo.gstbilling.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.*
import com.mimo.gstbilling.utils.BiometricHelper

@Composable
fun BiometricLockScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (BiometricHelper.isBiometricLockEnabled(context) && BiometricHelper.isBiometricAvailable(context)) {
            activity?.let {
                isAuthenticating = true
                BiometricHelper.showBiometricPrompt(
                    activity = it,
                    title = "Mimo GST Billing",
                    subtitle = "Verify your identity to unlock",
                    onSuccess = {
                        isAuthenticating = false
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.BiometricLock.route) { inclusive = true }
                        }
                    },
                    onError = { error ->
                        isAuthenticating = false
                        errorMessage = error
                    },
                    onFailed = {
                        errorMessage = "Authentication failed. Please try again."
                    }
                )
            }
        } else {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.BiometricLock.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lock",
                modifier = Modifier.size(60.dp),
                tint = Primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Mimo GST Billing",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "App is locked",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                errorMessage = null
                isAuthenticating = true
                activity?.let {
                    BiometricHelper.showBiometricPrompt(
                        activity = it,
                        title = "Mimo GST Billing",
                        subtitle = "Verify your identity to unlock",
                        onSuccess = {
                            isAuthenticating = false
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.BiometricLock.route) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            isAuthenticating = false
                            errorMessage = error
                        },
                        onFailed = {
                            isAuthenticating = false
                            errorMessage = "Authentication failed. Please try again."
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
            enabled = !isAuthenticating
        ) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = "Fingerprint",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isAuthenticating) "Authenticating..." else "Use Fingerprint",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.BiometricLock.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
        ) {
            Text(
                text = "Skip",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = Error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

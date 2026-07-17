package com.mimo.gstbilling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.mimo.gstbilling.ui.navigation.MimoNavHost
import com.mimo.gstbilling.ui.navigation.Screen
import com.mimo.gstbilling.ui.theme.MimoGstBillingTheme
import com.mimo.gstbilling.ui.theme.ThemeManager
import com.mimo.gstbilling.utils.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        enableEdgeToEdge()

        val startDestination = if (BiometricHelper.isBiometricLockEnabled(this)) {
            Screen.BiometricLock.route
        } else {
            Screen.Dashboard.route
        }

        setContent {
            MimoGstBillingTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    MimoNavHost(navController = navController, startDestination = startDestination)
                }
            }
        }
    }
}

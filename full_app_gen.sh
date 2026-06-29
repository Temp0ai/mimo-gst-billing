#!/bin/bash
PROJECT="/root/Downloads/mimo ai/MimoGstBilling/app/src/main/java/com/mimo/gstbilling"

# 1. AppDatabase
cat > "$PROJECT/data/local/AppDatabase.kt" << 'KTOFF'
package com.mimo.gstbilling.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mimo.gstbilling.data.local.dao.*
import com.mimo.gstbilling.data.local.entity.*

@Database(
    entities = [
        CompanyEntity::class,
        PartyEntity::class,
        ItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun partyDao(): PartyDao
    abstract fun itemDao(): ItemDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
}
KTOFF

# 2. MimoGstApplication
cat > "$PROJECT/MimoGstApplication.kt" << 'KTOFF'
package com.mimo.gstbilling

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MimoGstApplication : Application()
KTOFF

# 3. MainActivity
cat > "$PROJECT/MainActivity.kt" << 'KTOFF'
package com.mimo.gstbilling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mimo.gstbilling.ui.navigation.MimoNavHost
import com.mimo.gstbilling.ui.theme.MimoGstBillingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MimoGstBillingTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    MimoNavHost(navController = navController)
                }
            }
        }
    }
}
KTOFF

echo "Application files created!"

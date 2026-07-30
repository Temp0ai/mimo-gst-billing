package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mimo.gstbilling.ui.theme.*
import java.util.Calendar

data class SearchItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

private val allItems = listOf(
    SearchItem("Create Sale", "New sales invoice", Icons.Filled.AddShoppingCart, "create_invoice/sales"),
    SearchItem("Create Purchase", "New purchase invoice", Icons.Filled.ShoppingCart, "create_invoice/purchase"),
    SearchItem("Add Expense", "Record an expense", Icons.Filled.Receipt, "expenses"),
    SearchItem("View Stock", "Check inventory", Icons.Filled.Inventory, "items"),
    SearchItem("Parties", "Customers & suppliers", Icons.Filled.People, "parties"),
    SearchItem("Scan Invoice", "OCR scan paper bill", Icons.Filled.CameraAlt, "ocr_scan"),
    SearchItem("Reports", "Day book, cash flow", Icons.Filled.Assessment, "day_book_report"),
    SearchItem("GSTR-1 Report", "GST return filing", Icons.Filled.Description, "gstr1_report"),
    SearchItem("Smart Categorize", "AI expense categorization", Icons.Filled.AutoAwesome, "smart_category"),
    SearchItem("Quick Commands", "Natural language commands", Icons.Filled.RecordVoiceOver, "nl_command"),
    SearchItem("Settings", "App settings", Icons.Filled.Settings, "transaction_settings"),
    SearchItem("Bank Reconciliation", "Match bank statements", Icons.Filled.Settings, "ledger")
)

private fun getSmartSuggestion(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 6..11 -> "Good morning! Create a morning sale?"
        hour in 12..16 -> "Afternoon summary? View today's sales"
        hour in 17..21 -> "View today's expenses? Check pending payments"
        else -> "Welcome! What would you like to do?"
    }
}

private fun getSmartSuggestionRoute(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 6..11 -> "create_invoice/sales"
        hour in 12..16 -> "day_book_report"
        hour in 17..21 -> "expenses"
        else -> "dashboard"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    val filteredItems = remember(query) {
        if (query.isBlank()) emptyList()
        else allItems.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search", color = VyaparTopBarText, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VyaparTopBarIcon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VyaparWhite)
            )
        },
        containerColor = VyaparBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search actions, reports, settings...", color = VyaparSearchHint)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = VyaparIconDefault
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VyaparWhite,
                        unfocusedContainerColor = VyaparSearchBackground,
                        focusedBorderColor = VyaparInputFocused,
                        unfocusedBorderColor = VyaparSearchBorder,
                        cursorColor = VyaparInputFocused,
                        focusedTextColor = VyaparSearchText,
                        unfocusedTextColor = VyaparSearchText
                    ),
                    singleLine = true
                )
            }

            // Smart Suggestions (shown when search is empty)
            if (query.isBlank()) {
                item {
                    SectionHeader("Smart Suggestions")
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CardCorner.dp))
                            .clickable {
                                val route = getSmartSuggestionRoute()
                                navController.navigate(route)
                            },
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(VyaparLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = VyaparBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = getSmartSuggestion(),
                                color = VyaparTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Quick Actions
                item {
                    SectionHeader("Quick Actions")
                }
                item {
                    QuickActionsGrid(navController)
                }
            }

            // Search Results
            if (query.isNotBlank()) {
                if (filteredItems.isNotEmpty()) {
                    item {
                        SectionHeader("Results (${filteredItems.size})")
                    }
                    items(filteredItems) { item ->
                        SearchResultItem(item = item, navController = navController)
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No results found",
                                    color = VyaparTextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try a different search term",
                                    color = VyaparEmptyStateIcon,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = VyaparTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun QuickActionsGrid(navController: NavController) {
    val quickActions = listOf(
        Triple("Create Sale", Icons.Filled.AddShoppingCart, "create_invoice/sales"),
        Triple("Create Purchase", Icons.Filled.ShoppingCart, "create_invoice/purchase"),
        Triple("Add Expense", Icons.Filled.Receipt, "expenses"),
        Triple("View Stock", Icons.Filled.Inventory, "items"),
        Triple("Scan Invoice", Icons.Filled.CameraAlt, "ocr_scan")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        quickActions.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (title, icon, route) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(CardCorner.dp))
                            .clickable { navController.navigate(route) },
                        colors = CardDefaults.cardColors(containerColor = VyaparWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VyaparLightBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = VyaparBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = title,
                                color = VyaparTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // Fill remaining space if row is not complete
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(item: SearchItem, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardCorner.dp))
            .clickable { navController.navigate(item.route) },
        colors = CardDefaults.cardColors(containerColor = VyaparWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VyaparLightBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = VyaparBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = VyaparTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.subtitle,
                    color = VyaparTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

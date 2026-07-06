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
fun ItemSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("item_settings", Context.MODE_PRIVATE)

    var enableItem by remember { mutableStateOf(prefs.getBoolean("enable_item", true)) }
    var itemType by remember { mutableStateOf(prefs.getString("item_type", "Product") ?: "Product") }
    var barcodeScanning by remember { mutableStateOf(prefs.getBoolean("barcode_scanning", false)) }
    var stockMaintenance by remember { mutableStateOf(prefs.getBoolean("stock_maintenance", true)) }
    var manufacturing by remember { mutableStateOf(prefs.getBoolean("manufacturing", false)) }
    var itemUnits by remember { mutableStateOf(prefs.getBoolean("item_units", true)) }
    var defaultUnit by remember { mutableStateOf(prefs.getBoolean("default_unit", false)) }
    var itemCategory by remember { mutableStateOf(prefs.getBoolean("item_category", true)) }
    var partyWiseItemRate by remember { mutableStateOf(prefs.getBoolean("party_wise_item_rate", false)) }
    var wholesalePrice by remember { mutableStateOf(prefs.getBoolean("wholesale_price", false)) }
    var quantityDecimals by remember { mutableStateOf(prefs.getInt("quantity_decimals", 2)) }
    var itemWiseTax by remember { mutableStateOf(prefs.getBoolean("item_wise_tax", true)) }
    var calculateTaxBasedOnMrp by remember { mutableStateOf(prefs.getBoolean("calculate_tax_mrp", false)) }
    var itemWiseDiscount by remember { mutableStateOf(prefs.getBoolean("item_wise_discount", true)) }
    var updateSalePriceFromTxn by remember { mutableStateOf(prefs.getBoolean("update_sale_price_txn", false)) }
    var additionalItemFields by remember { mutableStateOf(prefs.getBoolean("additional_item_fields", false)) }
    var itemCustomFields by remember { mutableStateOf(prefs.getBoolean("item_custom_fields", false)) }
    var description by remember { mutableStateOf(prefs.getBoolean("description", false)) }
    var hsnSacCode by remember { mutableStateOf(prefs.getBoolean("hsn_sac_code", true)) }
    var additionalCess by remember { mutableStateOf(prefs.getBoolean("additional_cess", false)) }

    var showItemTypeDropdown by remember { mutableStateOf(false) }
    val itemTypes = listOf("Product", "Service", "Products and Services")

    fun savePref(key: String, value: Any) {
        prefs.edit().apply {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is String -> putString(key, value)
            }
            apply()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // Enable Item
                    SettingToggleRow(
                        title = "Enable Item",
                        checked = enableItem,
                        onCheckedChange = {
                            enableItem = it
                            savePref("enable_item", it)
                        }
                    )

                    // Item Type
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Item Type",
                                fontSize = 15.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedTextField(
                                value = itemType,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        modifier = Modifier.clickable { showItemTypeDropdown = true }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Divider,
                                    focusedBorderColor = Primary
                                )
                            )
                            DropdownMenu(
                                expanded = showItemTypeDropdown,
                                onDismissRequest = { showItemTypeDropdown = false }
                            ) {
                                itemTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            itemType = type
                                            savePref("item_type", type)
                                            showItemTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))

                    // Barcode scanning for items
                    SettingToggleRow(
                        title = "Barcode scanning for items",
                        checked = barcodeScanning,
                        onCheckedChange = {
                            barcodeScanning = it
                            savePref("barcode_scanning", it)
                        }
                    )

                    // Stock maintenance
                    SettingToggleRow(
                        title = "Stock maintenance",
                        checked = stockMaintenance,
                        onCheckedChange = {
                            stockMaintenance = it
                            savePref("stock_maintenance", it)
                        }
                    )

                    // Manufacturing (Premium)
                    SettingPremiumRow(
                        title = "Manufacturing",
                        checked = manufacturing,
                        onCheckedChange = {
                            manufacturing = it
                            savePref("manufacturing", it)
                        }
                    )

                    // Item Units
                    SettingToggleRow(
                        title = "Item Units",
                        checked = itemUnits,
                        onCheckedChange = {
                            itemUnits = it
                            savePref("item_units", it)
                        }
                    )

                    // Default Unit
                    SettingToggleRow(
                        title = "Default Unit",
                        checked = defaultUnit,
                        onCheckedChange = {
                            defaultUnit = it
                            savePref("default_unit", it)
                        }
                    )

                    // Item Category
                    SettingToggleRow(
                        title = "Item Category",
                        checked = itemCategory,
                        onCheckedChange = {
                            itemCategory = it
                            savePref("item_category", it)
                        }
                    )

                    // Party wise item rate (Premium)
                    SettingPremiumRow(
                        title = "Party wise item rate",
                        checked = partyWiseItemRate,
                        onCheckedChange = {
                            partyWiseItemRate = it
                            savePref("party_wise_item_rate", it)
                        }
                    )

                    // Wholesale Price (Premium)
                    SettingPremiumRow(
                        title = "Wholesale Price",
                        checked = wholesalePrice,
                        onCheckedChange = {
                            wholesalePrice = it
                            savePref("wholesale_price", it)
                        }
                    )

                    // Quantity (Decimal places) with stepper
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Quantity (Upto Decimal places)",
                                fontSize = 15.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = {
                                    if (quantityDecimals > 0) {
                                        quantityDecimals--
                                        savePref("quantity_decimals", quantityDecimals)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Remove,
                                    contentDescription = "Decrease",
                                    tint = Primary
                                )
                            }
                            Card(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = LightBlueBg)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        quantityDecimals.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (quantityDecimals < 6) {
                                        quantityDecimals++
                                        savePref("quantity_decimals", quantityDecimals)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Increase",
                                    tint = Primary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))

                    // Item wise tax
                    SettingToggleRow(
                        title = "Item wise tax",
                        checked = itemWiseTax,
                        onCheckedChange = {
                            itemWiseTax = it
                            savePref("item_wise_tax", it)
                        }
                    )

                    // Calculate Tax based on MRP
                    SettingToggleRow(
                        title = "Calculate Tax based on MRP",
                        checked = calculateTaxBasedOnMrp,
                        onCheckedChange = {
                            calculateTaxBasedOnMrp = it
                            savePref("calculate_tax_mrp", it)
                        }
                    )

                    // Item wise discount
                    SettingToggleRow(
                        title = "Item wise discount",
                        checked = itemWiseDiscount,
                        onCheckedChange = {
                            itemWiseDiscount = it
                            savePref("item_wise_discount", it)
                        }
                    )

                    // Update Sale Price from TXN
                    SettingToggleRow(
                        title = "Update Sale Price from TXN",
                        checked = updateSalePriceFromTxn,
                        onCheckedChange = {
                            updateSalePriceFromTxn = it
                            savePref("update_sale_price_txn", it)
                        }
                    )

                    // Additional Item Fields (Premium, navigable)
                    SettingNavigationRow(
                        title = "Additional Item Fields",
                        hasPremium = true,
                        onClick = {
                            additionalItemFields = !additionalItemFields
                            savePref("additional_item_fields", additionalItemFields)
                        }
                    )

                    // Item Custom Fields (Premium, navigable)
                    SettingNavigationRow(
                        title = "Item Custom Fields",
                        hasPremium = true,
                        onClick = {
                            itemCustomFields = !itemCustomFields
                            savePref("item_custom_fields", itemCustomFields)
                        }
                    )

                    // Description with pencil icon
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Description",
                                fontSize = 15.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = description,
                                onCheckedChange = {
                                    description = it
                                    savePref("description", it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFBDBDBD)
                                )
                            )
                        }
                    }
                }
            }

            // GST Section Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBlueBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    "GST",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            // GST Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // HSN/SAC Code
                    SettingToggleRow(
                        title = "HSN/SAC Code",
                        checked = hsnSacCode,
                        onCheckedChange = {
                            hsnSacCode = it
                            savePref("hsn_sac_code", it)
                        }
                    )

                    // Additional CESS
                    SettingToggleRow(
                        title = "Additional CESS",
                        checked = additionalCess,
                        onCheckedChange = {
                            additionalCess = it
                            savePref("additional_cess", it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
            Icon(
                Icons.Filled.Info,
                contentDescription = "Info",
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp)
            )
        }
        HorizontalDivider(
            color = Divider,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun SettingPremiumRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = "Premium",
                tint = Warning,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
            Icon(
                Icons.Filled.Info,
                contentDescription = "Info",
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp)
            )
        }
        HorizontalDivider(
            color = Divider,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun SettingNavigationRow(
    title: String,
    hasPremium: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (hasPremium) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = "Premium",
                    tint = Warning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        HorizontalDivider(
            color = Divider,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mimo.gstbilling.ui.theme.*

data class TransactionTypeItem(
    val title: String,
    val icon: ImageVector,
    val transactionType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTypeSheet(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val saleTransactions = listOf(
        TransactionTypeItem("Sale\nInvoices", Icons.Filled.Receipt, "sale_invoice"),
        TransactionTypeItem("Payment-In", Icons.Filled.ArrowDownward, "payment_in"),
        TransactionTypeItem("Cr. Note/\nSale Return", Icons.Filled.Note, "credit_note"),
        TransactionTypeItem("Sale\nOrder", Icons.Filled.Assignment, "sale_order"),
        TransactionTypeItem("Estimate/\nQuotation", Icons.Filled.Description, "estimate"),
        TransactionTypeItem("Delivery\nChallan", Icons.Filled.LocalShipping, "delivery_challan"),
        TransactionTypeItem("Mobile\nPOS", Icons.Filled.PointOfSale, "mobile_pos")
    )

    val purchaseTransactions = listOf(
        TransactionTypeItem("Purchase", Icons.Filled.ShoppingCart, "purchase"),
        TransactionTypeItem("Payment-Out", Icons.Filled.ArrowUpward, "payment_out"),
        TransactionTypeItem("Dr. Note/\nPurchase Return", Icons.Filled.Note, "debit_note"),
        TransactionTypeItem("Purchase\nOrder", Icons.Filled.Assignment, "purchase_order")
    )

    val otherTransactions = listOf(
        TransactionTypeItem("Expenses", Icons.Filled.AccountBalance, "expense"),
        TransactionTypeItem("Party To Party\nTransfer", Icons.Filled.SwapHoriz, "party_transfer")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Sale Transactions",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(saleTransactions) { item ->
                    TransactionTypeGridItem(item = item, onClick = {
                        onSelect(item.transactionType)
                        onDismiss()
                    })
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Purchase Transactions",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(purchaseTransactions) { item ->
                    TransactionTypeGridItem(item = item, onClick = {
                        onSelect(item.transactionType)
                        onDismiss()
                    })
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Other Transactions",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 150.dp)
            ) {
                items(otherTransactions) { item ->
                    TransactionTypeGridItem(item = item, onClick = {
                        onSelect(item.transactionType)
                        onDismiss()
                    })
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TextPrimary)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionTypeGridItem(
    item: TransactionTypeItem,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(LightBlueBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = item.title,
                tint = Primary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 11.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            maxLines = 2
        )
    }
}

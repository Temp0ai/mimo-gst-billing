package com.mimo.gstbilling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch

data class OnboardingPage(val title: String, val description: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = listOf(
        OnboardingPage("Welcome to Mimo GST Billing", "Your complete GST billing solution for Indian businesses. Create invoices, manage inventory, and file GST returns effortlessly.", Icons.Filled.Receipt, VyaparBlue),
        OnboardingPage("Create Professional Invoices", "Generate GST-compliant invoices with automatic tax calculations, HSN/SAC codes, and multiple template options.", Icons.Filled.Description, VyaparGreen),
        OnboardingPage("Manage Your Business", "Track parties, inventory, expenses, and payments all in one place. Get powerful reports and analytics.", Icons.Filled.Dashboard, Color(0xFFF57F17)),
        OnboardingPage("GST Filing Made Easy", "Auto-generate GSTR-1, GSTR-3B reports. Export data in GSTN JSON format for easy filing.", Icons.Filled.Verified, RedAccent)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val p = pages[page]
            Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(120.dp).background(p.color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(p.icon, contentDescription = null, tint = p.color, modifier = Modifier.size(60.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(p.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(p.description, fontSize = 16.sp, textAlign = TextAlign.Center, color = VyaparTextSecondary, lineHeight = 24.sp)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row { repeat(pages.size) { index -> Box(modifier = Modifier.padding(4.dp).size(if (pagerState.currentPage == index) 12.dp else 8.dp).clip(CircleShape).background(if (pagerState.currentPage == index) RedAccent else Color.LightGray)) } }
            Button(onClick = {
                if (pagerState.currentPage < pages.size - 1) { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                else { onComplete() }
            }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = RedAccent)) {
                Text(if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

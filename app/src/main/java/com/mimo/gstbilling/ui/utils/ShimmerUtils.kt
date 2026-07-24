package com.mimo.gstbilling.ui.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mimo.gstbilling.ui.theme.VyaparShimmerBackground
import com.mimo.gstbilling.ui.theme.VyaparShimmerHighlight

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shimmerColors: List<Color> = listOf(
        VyaparShimmerBackground,
        VyaparShimmerHighlight,
        VyaparShimmerBackground
    )
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
    )
}

@Composable
fun ShimmerListItem(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(150.dp)
                    .height(16.dp)
            )
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier
                .width(100.dp)
                .height(12.dp)
        )
    }
}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerBox(
            modifier = Modifier
                .width(120.dp)
                .height(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(14.dp)
            )
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
            )
        }
    }
}

@Composable
fun ShimmerDashboardSummary(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
        )
        ShimmerBox(
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
        )
    }
}

@Composable
fun ShimmerTabRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShimmerBox(
            modifier = Modifier
                .width(80.dp)
                .height(36.dp)
        )
        ShimmerBox(
            modifier = Modifier
                .width(100.dp)
                .height(36.dp)
        )
        ShimmerBox(
            modifier = Modifier
                .width(60.dp)
                .height(36.dp)
        )
    }
}

@Composable
fun ShimmerSearchBar(
    modifier: Modifier = Modifier
) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
    )
}

package com.feedshield.android.presentation.analytics.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feedshield.android.data.model.DailyStats
import com.feedshield.android.presentation.theme.*

@Composable
fun WeeklyBarChart(
    weeklyStats: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    val maxBlocks = (weeklyStats.maxOfOrNull { it.totalBlocks } ?: 1).coerceAtLeast(5)
    val totalWeeklyBlocks = weeklyStats.sumOf { it.totalBlocks }
    val todayDateStr = DailyStats.todayDateStr()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "7-Day Interception Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Daily blocked doom-scrolling attempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ShieldBluePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$totalWeeklyBlocks Total",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ShieldCyanAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bar Chart Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyStats.forEach { dayStats ->
                    val isToday = dayStats.dateStr == todayDateStr
                    val heightRatio = (dayStats.totalBlocks.toFloat() / maxBlocks.toFloat()).coerceIn(0.06f, 1f)

                    val animatedRatio by animateFloatAsState(
                        targetValue = heightRatio,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        label = "barHeight"
                    )

                    val barColor = if (isToday) {
                        Brush.verticalGradient(listOf(ShieldCyanAccent, ShieldGreenActive))
                    } else if (dayStats.totalBlocks > 0) {
                        Brush.verticalGradient(listOf(ShieldBluePrimary, ShieldBlueDark))
                    } else {
                        Brush.verticalGradient(listOf(DarkBorder, DarkBorder.copy(alpha = 0.5f)))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Count label above bar
                        Text(
                            text = if (dayStats.totalBlocks > 0) "${dayStats.totalBlocks}" else "0",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) ShieldCyanAccent else TextMuted,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Vertical Bar
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(fraction = animatedRatio * 0.78f)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(barColor)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label below bar
                        Text(
                            text = if (isToday) "Today" else dayStats.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) TextPrimary else TextMuted,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        border = BorderStroke(1.dp, BorderSubtle)
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
                        text = "7-Day Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = "Blocked scroll attempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(
                        text = "$totalWeeklyBlocks total",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bar Chart Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
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
                        Brush.verticalGradient(listOf(PureWhite, AccentWhite.copy(alpha = 0.7f)))
                    } else if (dayStats.totalBlocks > 0) {
                        Brush.verticalGradient(listOf(TextMuted, TextDim))
                    } else {
                        Brush.verticalGradient(listOf(BorderSubtle, BorderSubtle.copy(alpha = 0.3f)))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Count label
                        Text(
                            text = if (dayStats.totalBlocks > 0) "${dayStats.totalBlocks}" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) PureWhite else TextMuted,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(fraction = animatedRatio * 0.78f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label
                        Text(
                            text = if (isToday) "Today" else dayStats.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) PureWhite else TextDim,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

package com.feedshield.android.presentation.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.feedshield.android.presentation.analytics.components.AppBreakdownCard
import com.feedshield.android.presentation.analytics.components.StatCard
import com.feedshield.android.presentation.analytics.components.WeeklyBarChart
import com.feedshield.android.presentation.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val totalMinutes = uiState.lifetimeMinutesSaved
    val formattedTimeSaved = if (totalMinutes >= 60) {
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        "${hours}h ${mins}m"
    } else {
        "${totalMinutes}m"
    }

    val todayMinutes = uiState.todayStats.minutesSaved
    val formattedTodayTime = "${todayMinutes}m"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                color = PureWhite
            )
            Text(
                text = "Your focus and time reclaimed",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        // 3 Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Today",
                value = "${uiState.todayStats.totalBlocks}",
                subtitle = "$formattedTodayTime saved",
                icon = Icons.Default.Block,
                accentColor = PureWhite,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Saved",
                value = formattedTimeSaved,
                subtitle = "${uiState.lifetimeBlocksCount} total",
                icon = Icons.Default.HourglassBottom,
                accentColor = PureWhite,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Streak",
                value = "${uiState.currentStreakDays}d",
                subtitle = if (uiState.currentStreakDays > 0) "Active" else "Start today",
                icon = Icons.Default.Whatshot,
                accentColor = PureWhite,
                modifier = Modifier.weight(1f)
            )
        }

        // 7-Day Chart
        WeeklyBarChart(weeklyStats = uiState.weeklyStats)

        // Platform Breakdown
        AppBreakdownCard(weeklyStats = uiState.weeklyStats)

        // Milestones
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBlack),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                MilestoneItem(
                    title = "First Interception",
                    description = "Take back control of your first session",
                    isUnlocked = uiState.lifetimeBlocksCount >= 1,
                    icon = Icons.Default.Flag
                )

                MilestoneItem(
                    title = "3-Day Streak",
                    description = "Stay focused for 3 consecutive days",
                    isUnlocked = uiState.currentStreakDays >= 3,
                    icon = Icons.Default.Whatshot
                )

                MilestoneItem(
                    title = "Time Master",
                    description = "Save more than 1 hour of screen time",
                    isUnlocked = uiState.lifetimeMinutesSaved >= 60,
                    icon = Icons.Default.Timer
                )
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    title: String,
    description: String,
    isUnlocked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val contentAlpha = if (isUnlocked) 1f else 0.35f
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) SurfaceElevated else CardBlack,
        border = BorderStroke(1.dp, if (isUnlocked) BorderMedium else BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = contentAlpha),
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary.copy(alpha = contentAlpha)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted.copy(alpha = contentAlpha)
                )
            }
            if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

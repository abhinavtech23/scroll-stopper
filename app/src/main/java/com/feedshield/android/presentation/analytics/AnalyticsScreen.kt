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
import androidx.compose.ui.graphics.Color
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

    // Refresh data when screen resumes
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

    // Format minutes saved nicely (e.g. "1h 45m" or "45m")
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
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title Header
        Column {
            Text(
                text = "Wellbeing & Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Track your reclaimed time and focus streaks",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // 3 Key Wellbeing Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Today",
                value = "${uiState.todayStats.totalBlocks}",
                subtitle = "$formattedTodayTime saved",
                icon = Icons.Default.Block,
                accentColor = ShieldBluePrimary,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Time Saved",
                value = formattedTimeSaved,
                subtitle = "${uiState.lifetimeBlocksCount} blocks total",
                icon = Icons.Default.HourglassBottom,
                accentColor = ShieldGreenActive,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Streak",
                value = "${uiState.currentStreakDays}d",
                subtitle = if (uiState.currentStreakDays > 0) "🔥 Active" else "Start today",
                icon = Icons.Default.Whatshot,
                accentColor = ShieldAmberWarning,
                modifier = Modifier.weight(1f)
            )
        }

        // 7-Day Activity Chart
        WeeklyBarChart(weeklyStats = uiState.weeklyStats)

        // Platform Breakdown
        AppBreakdownCard(weeklyStats = uiState.weeklyStats)

        // Gamification Milestones Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "🏆 Focus Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Milestone 1
                MilestoneItem(
                    title = "First Interception",
                    description = "Take back control of your first doom-scroll session",
                    isUnlocked = uiState.lifetimeBlocksCount >= 1,
                    icon = "🌱"
                )

                // Milestone 2
                MilestoneItem(
                    title = "3-Day Focus Streak",
                    description = "Intercept mindless scrolling for 3 consecutive days",
                    isUnlocked = uiState.currentStreakDays >= 3,
                    icon = "🔥"
                )

                // Milestone 3
                MilestoneItem(
                    title = "Time Master (1+ Hour Saved)",
                    description = "Save more than 60 minutes of your life from infinite feeds",
                    isUnlocked = uiState.lifetimeMinutesSaved >= 60,
                    icon = "⏳"
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
    icon: String
) {
    val alpha = if (isUnlocked) 1f else 0.45f
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) ShieldBluePrimary.copy(alpha = 0.12f) else DarkSurfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (isUnlocked) ShieldCyanAccent.copy(alpha = 0.3f) else DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary.copy(alpha = alpha)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted.copy(alpha = alpha)
                )
            }
            if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = ShieldGreenActive,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

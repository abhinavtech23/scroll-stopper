package com.feedshield.android.presentation.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feedshield.android.data.repository.StatsRepository
import com.feedshield.android.presentation.onboarding.OnboardingViewModel
import com.feedshield.android.presentation.theme.*

@Composable
fun DashboardScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val statsRepository = remember { StatsRepository(context) }
    var todayStats by remember { mutableStateOf(statsRepository.getTodayStats()) }
    var currentStreak by remember { mutableStateOf(statsRepository.calculateCurrentStreak()) }

    LaunchedEffect(uiState.isServiceActive, uiState.isProtectionEnabled) {
        todayStats = statsRepository.getTodayStats()
        currentStreak = statsRepository.calculateCurrentStreak()
    }

    // Protection is truly active only when service is running AND protection is enabled AND not snoozed
    val isFullyProtected = uiState.isServiceActive && uiState.isProtectionEnabled && !uiState.isSnoozed
    val statusColor = when {
        !uiState.isServiceActive -> TextMuted
        !uiState.isProtectionEnabled -> TextMuted
        uiState.isSnoozed -> WarningAmber
        else -> ActiveGreen
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isFullyProtected) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Scroll Stopper",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PureWhite
                )
                Text(
                    text = "Digital wellbeing protection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, BorderMedium)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = when {
                            !uiState.isServiceActive -> "No Permission"
                            !uiState.isProtectionEnabled -> "Off"
                            uiState.isSnoozed -> "Paused"
                            else -> "Active"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Hero Card with ON/OFF toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBlack),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tappable shield icon — acts as ON/OFF when permission is granted
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .then(
                            if (uiState.isServiceActive) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { viewModel.toggleProtection() }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = when {
                            isFullyProtected -> PureWhite.copy(alpha = 0.10f)
                            uiState.isServiceActive && uiState.isProtectionEnabled && uiState.isSnoozed -> WarningAmber.copy(alpha = 0.08f)
                            else -> SurfaceElevated
                        },
                        border = BorderStroke(
                            2.dp,
                            when {
                                isFullyProtected -> PureWhite.copy(alpha = 0.4f)
                                uiState.isSnoozed -> WarningAmber.copy(alpha = 0.3f)
                                else -> BorderSubtle
                            }
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    !uiState.isServiceActive -> Icons.Default.PowerSettingsNew
                                    uiState.isSnoozed -> Icons.Default.Timer
                                    uiState.isProtectionEnabled -> Icons.Default.Shield
                                    else -> Icons.Default.PowerSettingsNew
                                },
                                contentDescription = null,
                                tint = when {
                                    isFullyProtected -> PureWhite
                                    uiState.isSnoozed -> WarningAmber
                                    else -> TextMuted
                                },
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = when {
                        !uiState.isServiceActive -> "Protection Off"
                        !uiState.isProtectionEnabled -> "Protection Off"
                        uiState.isSnoozed -> "Protection Paused"
                        else -> "Shield Active"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        !uiState.isServiceActive -> "Enable in Accessibility Settings to start"
                        !uiState.isProtectionEnabled -> "Tap the shield to turn on"
                        uiState.isSnoozed -> {
                            val mins = uiState.remainingSnoozeSeconds / 60
                            val secs = uiState.remainingSnoozeSeconds % 60
                            "Resumes in ${mins}m ${String.format("%02d", secs)}s"
                        }
                        else -> "Tap the shield to turn off"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!uiState.isServiceActive) {
                    // First time: send to Accessibility Settings
                    Button(
                        onClick = { viewModel.openAccessibilitySettings(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = RichBlack
                        )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable Protection", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Permission granted: show ON/OFF toggle + Snooze
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Main ON/OFF button
                        Button(
                            onClick = { viewModel.toggleProtection() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (uiState.isProtectionEnabled) {
                                ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = RichBlack
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = SurfaceElevated,
                                    contentColor = TextPrimary
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.isProtectionEnabled) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isProtectionEnabled) "ON" else "OFF",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Snooze button (only when protection is ON)
                        if (uiState.isProtectionEnabled) {
                            OutlinedButton(
                                onClick = { viewModel.toggleSnooze15Minutes() },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderMedium),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (uiState.isSnoozed) WarningAmber else TextPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSnoozed) Icons.Default.PlayArrow else Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.isSnoozed) "Resume" else "15m",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBlack,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "INTERCEPTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${todayStats.totalBlocks}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "~${todayStats.minutesSaved}m saved today",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBlack,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${currentStreak}d",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentStreak > 0) "Focus streak active" else "Start today",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Active Targets
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Protection Targets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                // Instagram
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceElevated
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Instagram Reels",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = if (uiState.isAllowSingleDmReels) "DM reels allowed, scrolls blocked" else "All reels blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    Text(
                        text = if (uiState.isInstagramEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isInstagramEnabled) PureWhite else TextDim
                    )
                }

                HorizontalDivider(color = BorderSubtle)

                // YouTube
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceElevated
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "YouTube Shorts",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Shorts blocked, normal videos active",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    Text(
                        text = if (uiState.isYouTubeEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isYouTubeEnabled) PureWhite else TextDim
                    )
                }
            }
        }

        // Privacy footer
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "100% on-device, zero data collection",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

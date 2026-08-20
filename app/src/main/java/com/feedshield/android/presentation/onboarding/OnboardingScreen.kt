package com.feedshield.android.presentation.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.feedshield.android.R
import com.feedshield.android.presentation.onboarding.components.DisclosureCard
import com.feedshield.android.presentation.onboarding.components.ServiceStatusIndicator
import com.feedshield.android.presentation.theme.*

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh service status whenever user returns from Android Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshServiceStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            Surface(
                color = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.openAccessibilitySettings(context) },
                        enabled = uiState.isDisclosureAcknowledged || uiState.isServiceActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isServiceActive) ShieldGreenActive else ShieldBluePrimary,
                            disabledContainerColor = DarkSurfaceVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isServiceActive) Icons.Default.Check else Icons.Default.Settings,
                                contentDescription = null
                            )
                            Text(
                                text = if (uiState.isServiceActive) {
                                    stringResource(R.string.btn_service_active)
                                } else {
                                    stringResource(R.string.btn_enable_service)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Hero Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(ShieldBluePrimary, ShieldCyanAccent)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Scroll Stopper",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }

            // Snooze Active Banner (if snoozed)
            if (uiState.isSnoozed) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ShieldAmberWarning.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, ShieldAmberWarning)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val mins = uiState.remainingSnoozeSeconds / 60
                        val secs = uiState.remainingSnoozeSeconds % 60
                        Column {
                            Text(
                                text = "⏸️ Protection Paused",
                                style = MaterialTheme.typography.labelLarge,
                                color = ShieldAmberWarning,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Resumes in ${mins}m ${String.format("%02d", secs)}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                        TextButton(
                            onClick = { viewModel.toggleSnooze15Minutes() }
                        ) {
                            Text(
                                text = "Resume",
                                color = ShieldCyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Live Service Status Banner
            ServiceStatusIndicator(isServiceActive = uiState.isServiceActive)

            // Mandatory Privacy & Accessibility Disclosure Card
            DisclosureCard(
                isAcknowledged = uiState.isDisclosureAcknowledged,
                onAcknowledgedChanged = { viewModel.onDisclosureAcknowledgedChanged(it) }
            )

            // Protection Controls & Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Interception Controls",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Snooze quick action
                        FilledTonalButton(
                            onClick = { viewModel.toggleSnooze15Minutes() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (uiState.isSnoozed) ShieldAmberWarning.copy(alpha = 0.2f) else DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isSnoozed) Icons.Default.PlayArrow else Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (uiState.isSnoozed) ShieldAmberWarning else ShieldCyanAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isSnoozed) "Resume" else "Snooze 15m",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.isSnoozed) ShieldAmberWarning else TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instagram Item
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instagram Reels Interceptor",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "DMs, Stories & Feed remain active",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = uiState.isInstagramEnabled,
                            onCheckedChange = { viewModel.onToggleInstagram(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ShieldBluePrimary
                            )
                        )
                    }

                    // Single DM Reel Option (Nested under Instagram)
                    if (uiState.isInstagramEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Allow Single DM Reels",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Watch reels shared in chat; blocks vertical swipe-scrolls",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                Checkbox(
                                    checked = uiState.isAllowSingleDmReels,
                                    onCheckedChange = { viewModel.onToggleAllowSingleDmReels(it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ShieldCyanAccent,
                                        checkmarkColor = DarkBackground
                                    )
                                )
                            }
                        }
                    }

                    Divider(
                        color = DarkBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // YouTube Item
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "YouTube Shorts Interceptor",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Regular videos & search remain active",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = uiState.isYouTubeEnabled,
                            onCheckedChange = { viewModel.onToggleYouTube(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ShieldBluePrimary
                            )
                        )
                    }

                    Divider(
                        color = DarkBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // Overlay HUD Notice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show HUD Notice on Intercept",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Displays floating 'Scroll Intercepted' card with quick snooze",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = uiState.isOverlayNoticeEnabled,
                            onCheckedChange = { viewModel.onToggleOverlayNotice(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ShieldBluePrimary
                            )
                        )
                    }
                }
            }

            // How It Works Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.how_it_works_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "• " + stringResource(R.string.how_it_works_1),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "• " + stringResource(R.string.how_it_works_2),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "• " + stringResource(R.string.how_it_works_3),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

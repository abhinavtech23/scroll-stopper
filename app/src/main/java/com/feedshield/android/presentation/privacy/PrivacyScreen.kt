package com.feedshield.android.presentation.privacy

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.feedshield.android.R
import com.feedshield.android.data.repository.StatsRepository
import com.feedshield.android.presentation.onboarding.OnboardingViewModel
import com.feedshield.android.presentation.theme.*

@Composable
fun PrivacyScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val statsRepository = remember { StatsRepository(context) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "Privacy & Data Safety",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "100% on-device transparency and Google Play compliance",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Quick Setup Guide Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, ShieldBluePrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.SettingsAccessibility,
                        contentDescription = null,
                        tint = ShieldCyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "How to Enable Scroll Stopper",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "1. Tap the button below to open Android Accessibility Settings.\n" +
                            "2. Select 'Installed Apps' or 'Downloaded Services'.\n" +
                            "3. Find 'Scroll Stopper Protection Engine' and toggle it ON.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                )

                Button(
                    onClick = { viewModel.openAccessibilitySettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldBluePrimary)
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Accessibility Settings", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Google Play Data Safety & Accessibility Policy Card
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = ShieldGreenActive,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.privacy_disclosure_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = stringResource(R.string.privacy_disclosure_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🛡️ Google Play Data Safety Guarantees:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ShieldGreenActive
                        )
                        Text(
                            text = "• Zero Data Collected: No user identifiers, device IDs, or telemetry.\n" +
                                    "• Zero Data Shared: No network calls or third-party SDKs.\n" +
                                    "• Accessibility Use: Strictly limited to inspecting short-video container resource IDs on Instagram & YouTube.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Local Data Management
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Local Storage & Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "All your interception counts and streaks are stored 100% locally on your phone's storage. You can wipe your stats at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Local Analytics")
                }
            }
        }

        // App Version Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scroll Stopper v1.2.0 • Production Ready",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Text(
                text = "Open-Source Digital Wellbeing Engine",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Clear All Analytics?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset your interception counts and streaks back to zero. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        statsRepository.resetAllStats()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Reset Stats")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

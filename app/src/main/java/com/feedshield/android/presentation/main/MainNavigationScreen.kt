package com.feedshield.android.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.feedshield.android.presentation.dashboard.DashboardScreen
import com.feedshield.android.presentation.onboarding.OnboardingViewModel
import com.feedshield.android.presentation.privacy.PrivacyScreen
import com.feedshield.android.presentation.rules.RulesScreen
import com.feedshield.android.presentation.theme.*

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Shield),
    RULES("Rules", Icons.Default.Tune),
    PRIVACY("Privacy", Icons.Default.Lock)
}

@Composable
fun MainNavigationScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh service status on resume
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

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(text = tab.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextPrimary,
                            selectedTextColor = ShieldCyanAccent,
                            indicatorColor = ShieldBluePrimary.copy(alpha = 0.35f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                NavigationTab.RULES -> RulesScreen(viewModel = viewModel)
                NavigationTab.PRIVACY -> PrivacyScreen(viewModel = viewModel)
            }
        }
    }
}

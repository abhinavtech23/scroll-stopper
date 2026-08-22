package com.feedshield.android.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feedshield.android.presentation.analytics.AnalyticsScreen
import com.feedshield.android.presentation.analytics.AnalyticsViewModel
import com.feedshield.android.presentation.dashboard.DashboardScreen
import com.feedshield.android.presentation.onboarding.OnboardingViewModel
import com.feedshield.android.presentation.privacy.PrivacyScreen
import com.feedshield.android.presentation.rules.RulesScreen
import com.feedshield.android.presentation.theme.*

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Shield),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    RULES("Rules", Icons.Default.Tune),
    PRIVACY("Privacy", Icons.Default.Lock)
}

@Composable
fun MainNavigationScreen(
    viewModel: OnboardingViewModel,
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh service status & stats on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshServiceStatus()
                analyticsViewModel.refreshStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = RichBlack,
        bottomBar = {
            NavigationBar(
                containerColor = CardBlack,
                tonalElevation = 0.dp
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
                            selectedIconColor = PureWhite,
                            selectedTextColor = PureWhite,
                            indicatorColor = SurfaceElevated,
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
                NavigationTab.ANALYTICS -> AnalyticsScreen(viewModel = analyticsViewModel)
                NavigationTab.RULES -> RulesScreen(viewModel = viewModel)
                NavigationTab.PRIVACY -> PrivacyScreen(viewModel = viewModel)
            }
        }
    }
}

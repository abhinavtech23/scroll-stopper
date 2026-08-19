package com.feedshield.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.feedshield.android.presentation.onboarding.OnboardingScreen
import com.feedshield.android.presentation.onboarding.OnboardingViewModel
import com.feedshield.android.presentation.theme.FeedShieldTheme

/**
 * Main Activity serving as the entry point and onboarding host for FeedShield.
 */
class MainActivity : ComponentActivity() {

    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FeedShieldTheme {
                OnboardingScreen(viewModel = onboardingViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        onboardingViewModel.refreshServiceStatus()
    }
}

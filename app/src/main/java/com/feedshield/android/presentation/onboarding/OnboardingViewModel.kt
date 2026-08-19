package com.feedshield.android.presentation.onboarding

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feedshield.android.core.accessibility.FeedShieldAccessibilityService
import com.feedshield.android.core.util.AccessibilityUtils
import com.feedshield.android.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isServiceActive: Boolean = false,
    val isDisclosureAcknowledged: Boolean = false,
    val isInstagramEnabled: Boolean = true,
    val isYouTubeEnabled: Boolean = true
)

class OnboardingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            isServiceActive = checkServiceStatus(),
            isDisclosureAcknowledged = repository.isDisclosureAcknowledged,
            isInstagramEnabled = repository.isInstagramBlocked,
            isYouTubeEnabled = repository.isYouTubeBlocked
        )
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun refreshServiceStatus() {
        _uiState.update {
            it.copy(isServiceActive = checkServiceStatus())
        }
    }

    fun onDisclosureAcknowledgedChanged(acknowledged: Boolean) {
        repository.isDisclosureAcknowledged = acknowledged
        _uiState.update {
            it.copy(isDisclosureAcknowledged = acknowledged)
        }
    }

    fun onToggleInstagram(enabled: Boolean) {
        repository.isInstagramBlocked = enabled
        _uiState.update { it.copy(isInstagramEnabled = enabled) }
    }

    fun onToggleYouTube(enabled: Boolean) {
        repository.isYouTubeBlocked = enabled
        _uiState.update { it.copy(isYouTubeEnabled = enabled) }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = AccessibilityUtils.getAccessibilitySettingsIntent()
        context.startActivity(intent)
    }

    private fun checkServiceStatus(): Boolean {
        return AccessibilityUtils.isAccessibilityServiceEnabled(
            getApplication(),
            FeedShieldAccessibilityService::class.java
        ) || FeedShieldAccessibilityService.isServiceRunning
    }
}

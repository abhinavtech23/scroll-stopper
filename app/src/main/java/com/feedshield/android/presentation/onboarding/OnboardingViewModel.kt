package com.feedshield.android.presentation.onboarding

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feedshield.android.core.accessibility.FeedShieldAccessibilityService
import com.feedshield.android.core.util.AccessibilityUtils
import com.feedshield.android.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isServiceActive: Boolean = false,
    val isDisclosureAcknowledged: Boolean = false,
    val isInstagramEnabled: Boolean = true,
    val isYouTubeEnabled: Boolean = true,
    val isAllowSingleDmReels: Boolean = true,
    val isSnoozed: Boolean = false,
    val remainingSnoozeSeconds: Long = 0L,
    val isOverlayNoticeEnabled: Boolean = true
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
            isYouTubeEnabled = repository.isYouTubeBlocked,
            isAllowSingleDmReels = repository.isAllowSingleDmReels,
            isSnoozed = repository.isSnoozed(),
            remainingSnoozeSeconds = repository.getRemainingSnoozeSeconds(),
            isOverlayNoticeEnabled = repository.isOverlayNoticeEnabled
        )
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        // Periodic ticker for Snooze countdown display
        viewModelScope.launch {
            while (isActive) {
                val snoozed = repository.isSnoozed()
                val remaining = repository.getRemainingSnoozeSeconds()
                _uiState.update {
                    it.copy(
                        isSnoozed = snoozed,
                        remainingSnoozeSeconds = remaining
                    )
                }
                delay(1000)
            }
        }
    }

    fun refreshServiceStatus() {
        _uiState.update {
            it.copy(
                isServiceActive = checkServiceStatus(),
                isSnoozed = repository.isSnoozed(),
                remainingSnoozeSeconds = repository.getRemainingSnoozeSeconds()
            )
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

    fun onToggleAllowSingleDmReels(enabled: Boolean) {
        repository.isAllowSingleDmReels = enabled
        _uiState.update { it.copy(isAllowSingleDmReels = enabled) }
    }

    fun onToggleOverlayNotice(enabled: Boolean) {
        repository.isOverlayNoticeEnabled = enabled
        _uiState.update { it.copy(isOverlayNoticeEnabled = enabled) }
    }

    fun toggleSnooze15Minutes() {
        if (repository.isSnoozed()) {
            repository.cancelSnooze()
        } else {
            repository.snoozeForMinutes(15)
        }
        refreshServiceStatus()
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

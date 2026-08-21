package com.feedshield.android.core.accessibility.interceptor

import android.accessibilityservice.AccessibilityService
import com.feedshield.android.core.accessibility.detector.DetectionResult
import com.feedshield.android.core.accessibility.overlay.ScrollStopperOverlayManager
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository
import com.feedshield.android.data.repository.StatsRepository

/**
 * Production-ready interceptor that handles back actions, overlay notices, snooze checks,
 * and records gamification wellbeing statistics.
 */
class DefaultShortVideoInterceptor(
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository? = null,
    private val overlayManager: ScrollStopperOverlayManager? = null,
    private val autoBackActionEnabled: Boolean = true
) : ShortVideoInterceptor {

    companion object {
        private const val TAG = "ScrollStopper.Interceptor"
        private const val BACK_ACTION_COOLDOWN_MS = 600L
    }

    private var lastInterceptionTime = 0L

    override fun onShortVideoDetected(service: AccessibilityService, result: DetectionResult) {
        // Check if snooze mode is active
        if (settingsRepository.isSnoozed()) {
            val remaining = settingsRepository.getRemainingSnoozeSeconds()
            Logger.d(TAG, "Protection is currently snoozed (${remaining}s remaining). Skipping interception.")
            return
        }

        // Throttle back actions to avoid rapid double-taps
        val now = System.currentTimeMillis()
        if (now - lastInterceptionTime < BACK_ACTION_COOLDOWN_MS) {
            return
        }
        lastInterceptionTime = now

        // Record interception event in local analytics storage
        try {
            statsRepository?.recordInterception(result.targetPackage, result.reason)
        } catch (e: Exception) {
            Logger.e(TAG, "Error recording stats: ${e.message}", e)
        }

        Logger.i(
            TAG,
            "🎯 Intercepting short video: ${result.featureType} in ${result.targetPackage} " +
                    "(Reason: ${result.reason}, Container: ${result.matchedContainerId})"
        )

        // Show visual HUD notice if enabled
        if (settingsRepository.isOverlayNoticeEnabled) {
            overlayManager?.showInterceptionNotice(result)
        }

        // Trigger Android Back Action to exit the short-video container
        if (autoBackActionEnabled) {
            Logger.i(TAG, "Executing GLOBAL_ACTION_BACK to exit short video container.")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }
}

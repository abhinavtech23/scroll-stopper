package com.feedshield.android.core.accessibility.interceptor

import android.accessibilityservice.AccessibilityService
import com.feedshield.android.core.accessibility.detector.DetectionResult
import com.feedshield.android.core.accessibility.overlay.ScrollStopperOverlayManager
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository
import com.feedshield.android.data.repository.StatsRepository

/**
 * Production-ready interceptor that presents the full-screen blur focus overlay,
 * records gamification wellbeing statistics, and handles snooze checks.
 *
 * Automatic back navigation is disabled by default to keep the user on screen while
 * blurring the reel and showing the motivational intervention.
 */
class DefaultShortVideoInterceptor(
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository? = null,
    private val overlayManager: ScrollStopperOverlayManager? = null,
    private val autoBackActionEnabled: Boolean = false
) : ShortVideoInterceptor {

    companion object {
        private const val TAG = "ScrollStopper.Interceptor"
        private const val INTERCEPTION_COOLDOWN_MS = 800L
    }

    private var lastInterceptionTime = 0L

    override fun onShortVideoDetected(service: AccessibilityService, result: DetectionResult) {
        // Check if snooze mode is active
        if (settingsRepository.isSnoozed()) {
            val remaining = settingsRepository.getRemainingSnoozeSeconds()
            Logger.d(TAG, "Protection is currently snoozed (${remaining}s remaining). Skipping interception.")
            return
        }

        // Throttle rapid duplicate detections
        val now = System.currentTimeMillis()
        if (now - lastInterceptionTime < INTERCEPTION_COOLDOWN_MS) {
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

        // Show visual full-screen blur motivation overlay if enabled
        if (settingsRepository.isOverlayNoticeEnabled) {
            overlayManager?.showInterceptionNotice(result)
        }

        // Optional Android Back Action: disabled so user is NOT kicked back to the old screen
        if (autoBackActionEnabled) {
            Logger.i(TAG, "Executing GLOBAL_ACTION_BACK to exit short video container.")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }
}

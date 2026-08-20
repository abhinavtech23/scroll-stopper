package com.feedshield.android.core.accessibility.interceptor

import android.accessibilityservice.AccessibilityService
import com.feedshield.android.core.accessibility.detector.DetectionResult
import com.feedshield.android.core.accessibility.overlay.ScrollStopperOverlayManager
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository

/**
 * Production-ready interceptor that handles back actions, overlay notices, and snooze checks.
 */
class DefaultShortVideoInterceptor(
    private val settingsRepository: SettingsRepository,
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

        // Throttle back actions to avoid repeated rapid presses
        val now = System.currentTimeMillis()
        if (now - lastInterceptionTime < BACK_ACTION_COOLDOWN_MS) {
            return
        }
        lastInterceptionTime = now

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

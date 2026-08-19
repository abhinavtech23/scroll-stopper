package com.feedshield.android.core.accessibility.interceptor

import android.accessibilityservice.AccessibilityService
import com.feedshield.android.core.accessibility.detector.DetectionResult
import com.feedshield.android.core.util.Logger

/**
 * Default implementation of ShortVideoInterceptor.
 * Logs detections and prepares interception hooks (such as performing Back action or notifying UI).
 */
class DefaultShortVideoInterceptor(
    private val autoBackActionEnabled: Boolean = false
) : ShortVideoInterceptor {

    companion object {
        private const val TAG = "FeedShield.Interceptor"
    }

    override fun onShortVideoDetected(service: AccessibilityService, result: DetectionResult) {
        Logger.i(
            TAG,
            "🎯 Short video intercepted: ${result.featureType} in ${result.targetPackage} " +
                    "(Container: ${result.matchedContainerId}, Confidence: ${result.confidence})"
        )

        if (autoBackActionEnabled) {
            Logger.i(TAG, "Triggering automatic GLOBAL_ACTION_BACK to exit short video container.")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }
}

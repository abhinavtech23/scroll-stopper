package com.feedshield.android.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.feedshield.android.core.accessibility.detector.InstagramReelsDetector
import com.feedshield.android.core.accessibility.detector.ShortVideoDetector
import com.feedshield.android.core.accessibility.detector.YouTubeShortsDetector
import com.feedshield.android.core.accessibility.interceptor.DefaultShortVideoInterceptor
import com.feedshield.android.core.accessibility.interceptor.ShortVideoInterceptor
import com.feedshield.android.core.util.Logger

/**
 * Core Accessibility Service Engine for FeedShield.
 *
 * Responsibilities:
 * - Efficiently processes accessibility events from target apps (Instagram & YouTube).
 * - Debounces rapid scroll & content updates to preserve battery and maintain high performance.
 * - Delegates inspection to registered [ShortVideoDetector] strategies.
 * - Dispatches detection results to [ShortVideoInterceptor] implementations.
 * - Strictly operates 100% on-device with zero data retention or telemetry.
 */
class FeedShieldAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FeedShield.AccessibilityService"
        private const val DEBOUNCE_INTERVAL_MS = 150L

        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }

    private val detectors: List<ShortVideoDetector> by lazy {
        listOf(
            InstagramReelsDetector(),
            YouTubeShortsDetector()
        )
    }

    private val interceptor: ShortVideoInterceptor by lazy {
        DefaultShortVideoInterceptor(autoBackActionEnabled = false)
    }

    private var lastProcessedTimestamp: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Logger.i(TAG, "🛡️ FeedShield Accessibility Service connected successfully.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // Filter out non-monitored packages early
        val detector = detectors.firstOrNull { it.targetPackage == packageName } ?: return

        // Throttle / debounce high-frequency scroll and layout events
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedTimestamp < DEBOUNCE_INTERVAL_MS) {
            return
        }
        lastProcessedTimestamp = currentTime

        try {
            val rootNode = rootInActiveWindow ?: return

            // Run detection strategy on active view hierarchy
            val result = detector.detect(rootNode, event)
            if (result != null) {
                Logger.i(TAG, "Intercepted short video layout: ${result.featureType} (${result.matchedContainerId})")
                interceptor.onShortVideoDetected(this, result)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during accessibility event handling for $packageName: ${e.message}", e)
        }
    }

    override fun onInterrupt() {
        Logger.w(TAG, "FeedShield Accessibility Service interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Logger.i(TAG, "FeedShield Accessibility Service destroyed.")
    }
}

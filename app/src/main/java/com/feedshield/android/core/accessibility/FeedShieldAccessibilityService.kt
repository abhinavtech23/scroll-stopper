package com.feedshield.android.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.feedshield.android.core.accessibility.detector.InstagramReelsDetector
import com.feedshield.android.core.accessibility.detector.ShortVideoDetector
import com.feedshield.android.core.accessibility.detector.YouTubeShortsDetector
import com.feedshield.android.core.accessibility.interceptor.DefaultShortVideoInterceptor
import com.feedshield.android.core.accessibility.interceptor.ShortVideoInterceptor
import com.feedshield.android.core.accessibility.overlay.ScrollStopperOverlayManager
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository

/**
 * Ultra-responsive, lag-free Accessibility Service Engine for Scroll Stopper.
 *
 * Performance Optimizations:
 * - Early event rejection to avoid tree traversal on standard text/tap/animation events.
 * - Dynamic debouncing: Scroll events are evaluated immediately; content updates are throttled.
 * - Zero background memory leaks or IPC blocking.
 */
class FeedShieldAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ScrollStopper.Service"
        private const val CONTENT_CHANGE_DEBOUNCE_MS = 150L

        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }

    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val overlayManager: ScrollStopperOverlayManager by lazy {
        ScrollStopperOverlayManager(this, settingsRepository)
    }

    private val detectors: List<ShortVideoDetector> by lazy {
        listOf(
            InstagramReelsDetector(settingsRepository),
            YouTubeShortsDetector(settingsRepository)
        )
    }

    private val interceptor: ShortVideoInterceptor by lazy {
        DefaultShortVideoInterceptor(
            settingsRepository = settingsRepository,
            overlayManager = overlayManager,
            autoBackActionEnabled = true
        )
    }

    private var lastContentChangeTimestamp: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Logger.i(TAG, "🛡️ Scroll Stopper service connected and optimized for performance.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // Filter out non-target apps immediately
        val detector = detectors.firstOrNull { it.targetPackage == packageName } ?: return

        // Skip if user snoozed protection
        if (settingsRepository.isSnoozed()) {
            return
        }

        val eventType = event.eventType

        // Throttle minor content changes to prevent freezing during like animations or comments
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            val now = System.currentTimeMillis()
            if (now - lastContentChangeTimestamp < CONTENT_CHANGE_DEBOUNCE_MS) {
                return
            }
            lastContentChangeTimestamp = now
        }

        try {
            val rootNode = rootInActiveWindow

            // Run detection strategy
            val result = detector.detect(rootNode, event)
            if (result != null) {
                Logger.i(
                    TAG,
                    "🎯 Short video intercepted: ${result.featureType} - Reason: ${result.reason}"
                )
                interceptor.onShortVideoDetected(this, result)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error processing event for $packageName: ${e.message}", e)
        }
    }

    override fun onInterrupt() {
        Logger.w(TAG, "Scroll Stopper service interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        overlayManager.destroy()
        Logger.i(TAG, "Scroll Stopper service destroyed.")
    }
}

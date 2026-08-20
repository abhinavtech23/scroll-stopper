package com.feedshield.android.core.accessibility.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.feedshield.android.core.accessibility.NodeInspector
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository

/**
 * High-performance, stabilized detector for Instagram Reels.
 *
 * Implements a 3-State Session Machine:
 * 1. INITIAL_OPEN: User opens a Reel from DM/Link -> Creates session with a 1.8s grace period to allow video load & viewpager setup.
 * 2. WATCHING_SINGLE_REEL: User can watch, replay, like, and comment freely on this single Reel without interruption.
 * 3. SWIPE_INTERCEPTED: Only when an actual vertical swipe-down is executed after the initial load is `INSTAGRAM_REELS_SWIPE_PREVENTED` triggered.
 */
class InstagramReelsDetector(
    private val settingsRepository: SettingsRepository
) : ShortVideoDetector {

    override val targetPackage: String = INSTAGRAM_PACKAGE

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val TAG = "ScrollStopper.Instagram"

        private val REELS_CONTAINER_VIEW_IDS = setOf(
            "clips_viewer_view_pager",
            "clips_video_container",
            "reel_viewer_container",
            "clips_swipe_refresh_layout",
            "clips_item_container",
            "reel_recycler_view",
            "clips_viewer_fragment_container"
        )

        // Grace period in ms after opening a Reel to ignore initial ViewPager layout & attach events
        private const val INITIAL_LOAD_GRACE_PERIOD_MS = 1800L
        private const val SESSION_MAX_IDLE_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
    }

    private data class ReelSession(
        val sessionId: Long = System.currentTimeMillis(),
        val startTime: Long = System.currentTimeMillis(),
        var initialReelViewed: Boolean = true,
        var swipeDetected: Boolean = false
    )

    private var activeSession: ReelSession? = null

    override fun detect(rootNode: AccessibilityNodeInfo?, event: AccessibilityEvent): DetectionResult? {
        if (!settingsRepository.isInstagramBlocked) {
            return null
        }

        val eventType = event.eventType

        try {
            // Fast source check to see if the event relates to Reels
            val sourceId = event.source?.viewIdResourceName
            val isDirectReelSource = sourceId != null && REELS_CONTAINER_VIEW_IDS.any {
                sourceId.endsWith(it, ignoreCase = true)
            }

            // If it's a minor content change not related to reels, skip expensive tree searches
            if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && !isDirectReelSource && activeSession == null) {
                return null
            }

            // Check if Reels container is present
            val isReelsActive = isDirectReelSource || isReelsContainerPresent(rootNode)

            if (!isReelsActive) {
                // User is not in Reels viewer -> Reset session cleanly
                if (activeSession != null) {
                    Logger.d(TAG, "User exited Reels view. Resetting active session.")
                    activeSession = null
                }
                return null
            }

            val matchedId = sourceId ?: "clips_viewer_view_pager"

            // If user disabled Single DM Reel mode, intercept immediately
            if (!settingsRepository.isAllowSingleDmReels) {
                Logger.d(TAG, "DM allowance disabled. Intercepting Reel immediately.")
                return DetectionResult(
                    targetPackage = targetPackage,
                    featureType = "Instagram Reels",
                    matchedContainerId = matchedId,
                    reason = InterceptionReason.INSTAGRAM_REELS_IMMEDIATE,
                    confidence = 1.0f
                )
            }

            // Single DM Reel Mode is ENABLED:
            val currentSession = activeSession

            if (currentSession == null) {
                // First time opening the Reel -> Start session & grant initial grace period
                activeSession = ReelSession()
                Logger.i(TAG, "🎬 Single DM Reel opened. Grace period active - watching allowed.")
                return null
            }

            val sessionDuration = System.currentTimeMillis() - currentSession.startTime

            // During initial load grace period (1.8s), ignore all layout & scroll initialization events
            if (sessionDuration < INITIAL_LOAD_GRACE_PERIOD_MS) {
                return null
            }

            // After grace period: Listen specifically for vertical swipe gestures
            if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED && isReelsActive) {
                if (!currentSession.swipeDetected) {
                    currentSession.swipeDetected = true
                    Logger.i(TAG, "🛑 User swiped to next Reel! Intercepting scroll.")
                    return DetectionResult(
                        targetPackage = targetPackage,
                        featureType = "Instagram Reels",
                        matchedContainerId = matchedId,
                        reason = InterceptionReason.INSTAGRAM_REELS_SWIPE_PREVENTED,
                        confidence = 1.0f
                    )
                }
            }

            // Timeout reset if session is older than 10 minutes
            if (sessionDuration > SESSION_MAX_IDLE_TIMEOUT_MS) {
                activeSession = null
            }

        } catch (e: Exception) {
            Logger.e(TAG, "Error in Instagram detector: ${e.message}", e)
        }

        return null
    }

    private fun isReelsContainerPresent(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false
        return NodeInspector.hasNodeWithIdSuffix(rootNode, REELS_CONTAINER_VIEW_IDS, maxDepth = 15)
    }

    fun resetSession() {
        activeSession = null
    }
}

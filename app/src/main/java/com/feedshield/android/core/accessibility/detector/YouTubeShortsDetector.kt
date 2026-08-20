package com.feedshield.android.core.accessibility.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.feedshield.android.core.accessibility.NodeInspector
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository

/**
 * Detector targeting YouTube Shorts player and container views.
 * Immediately intercepts YouTube Shorts while keeping regular video playback, search, and home feed usable.
 */
class YouTubeShortsDetector(
    private val settingsRepository: SettingsRepository
) : ShortVideoDetector {

    override val targetPackage: String = YOUTUBE_PACKAGE

    companion object {
        const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val TAG = "ScrollStopper.YouTube"

        // Known View IDs and tag signatures for YouTube Shorts containers
        private val SHORTS_CONTAINER_VIEW_IDS = setOf(
            "reel_player_page_container",
            "reel_player_view_holder",
            "reel_recycler",
            "shorts_container",
            "shorts_player_fragment",
            "reel_view_pager",
            "reel_player_surface_view"
        )
    }

    override fun detect(rootNode: AccessibilityNodeInfo?, event: AccessibilityEvent): DetectionResult? {
        if (!settingsRepository.isYouTubeBlocked) {
            return null
        }

        if (rootNode == null) return null

        try {
            // Check direct event source view ID first for fast-path detection
            val sourceId = event.source?.viewIdResourceName
            if (sourceId != null && SHORTS_CONTAINER_VIEW_IDS.any { sourceId.endsWith(it, ignoreCase = true) }) {
                Logger.d(TAG, "Fast-path Shorts detection via source viewId: $sourceId")
                return DetectionResult(
                    targetPackage = targetPackage,
                    featureType = "YouTube Shorts",
                    matchedContainerId = sourceId,
                    reason = InterceptionReason.YOUTUBE_SHORTS,
                    confidence = 1.0f
                )
            }

            // Inspect hierarchy for short-video container signatures
            val matchedNode = NodeInspector.findFirstMatching(rootNode, maxDepth = 25) { node ->
                val viewId = node.viewIdResourceName ?: return@findFirstMatching false
                SHORTS_CONTAINER_VIEW_IDS.any { suffix -> viewId.endsWith(suffix, ignoreCase = true) }
            }

            if (matchedNode != null) {
                val matchedId = matchedNode.viewIdResourceName ?: "unknown_shorts_container"
                Logger.d(TAG, "Detected YouTube Shorts container: $matchedId")
                return DetectionResult(
                    targetPackage = targetPackage,
                    featureType = "YouTube Shorts",
                    matchedContainerId = matchedId,
                    reason = InterceptionReason.YOUTUBE_SHORTS,
                    confidence = 0.95f
                )
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error evaluating YouTube node hierarchy: ${e.message}", e)
        }

        return null
    }
}

package com.feedshield.android.core.accessibility.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.feedshield.android.core.accessibility.NodeInspector
import com.feedshield.android.core.util.Logger

/**
 * Detector targeting Instagram Reels short-video viewer containers.
 * Differentiates Reels viewers from regular feed posts, Stories, and Direct Messages.
 */
class InstagramReelsDetector : ShortVideoDetector {

    override val targetPackage: String = INSTAGRAM_PACKAGE

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val TAG = "FeedShield.Instagram"

        // Known View IDs associated with Instagram Reels full-screen scroll containers
        private val REELS_CONTAINER_VIEW_IDS = setOf(
            "clips_viewer_view_pager",
            "clips_video_container",
            "reel_viewer_container",
            "clips_swipe_refresh_layout",
            "reel_recycler_view",
            "clips_item_container",
            "clips_viewer_fragment_container"
        )
    }

    override fun detect(rootNode: AccessibilityNodeInfo?, event: AccessibilityEvent): DetectionResult? {
        if (rootNode == null) return null

        try {
            // Check direct event source view ID first for fast-path detection
            val sourceId = event.source?.viewIdResourceName
            if (sourceId != null && REELS_CONTAINER_VIEW_IDS.any { sourceId.endsWith(it, ignoreCase = true) }) {
                Logger.d(TAG, "Fast-path Reels detection via source viewId: $sourceId")
                return DetectionResult(
                    targetPackage = targetPackage,
                    featureType = "Instagram Reels",
                    matchedContainerId = sourceId,
                    confidence = 1.0f
                )
            }

            // Inspect hierarchy for short-video container signatures
            val matchedNode = NodeInspector.findFirstMatching(rootNode, maxDepth = 25) { node ->
                val viewId = node.viewIdResourceName ?: return@findFirstMatching false
                REELS_CONTAINER_VIEW_IDS.any { suffix -> viewId.endsWith(suffix, ignoreCase = true) }
            }

            if (matchedNode != null) {
                val matchedId = matchedNode.viewIdResourceName ?: "unknown_reels_container"
                Logger.d(TAG, "Detected Instagram Reels container: $matchedId")
                return DetectionResult(
                    targetPackage = targetPackage,
                    featureType = "Instagram Reels",
                    matchedContainerId = matchedId,
                    confidence = 0.95f
                )
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error evaluating Instagram node hierarchy: ${e.message}", e)
        }

        return null
    }
}

package com.feedshield.android.core.accessibility.detector

/**
 * Categorization of why a short-video interception was triggered.
 */
enum class InterceptionReason {
    /**
     * User opened YouTube Shorts directly or through Shorts tab.
     */
    YOUTUBE_SHORTS,

    /**
     * User opened Instagram Reels with Single DM Reel allowance disabled.
     */
    INSTAGRAM_REELS_IMMEDIATE,

    /**
     * User viewed a single Instagram Reel and attempted to swipe/scroll to subsequent Reels.
     */
    INSTAGRAM_REELS_SWIPE_PREVENTED
}

/**
 * Data model encapsulating short-form video UI detection results and interception reasons.
 *
 * @property targetPackage The package name where detection occurred (e.g., com.instagram.android).
 * @property featureType Human-readable feature name (e.g., "Instagram Reels", "YouTube Shorts").
 * @property matchedContainerId The resource ID or signature of the matching short-video container.
 * @property reason The specific cause of interception.
 * @property confidence Detection confidence level (0.0 - 1.0).
 * @property timestamp System timestamp in milliseconds when detected.
 */
data class DetectionResult(
    val targetPackage: String,
    val featureType: String,
    val matchedContainerId: String,
    val reason: InterceptionReason,
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

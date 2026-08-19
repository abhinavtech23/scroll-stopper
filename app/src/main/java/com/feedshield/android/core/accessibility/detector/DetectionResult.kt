package com.feedshield.android.core.accessibility.detector

/**
 * Data model encapsulating short-form video UI detection results.
 *
 * @property targetPackage The package name where detection occurred (e.g., com.instagram.android).
 * @property featureType Human-readable feature name (e.g., "Instagram Reels", "YouTube Shorts").
 * @property matchedContainerId The resource ID or signature of the matching short-video container.
 * @property confidence Detection confidence level (0.0 - 1.0).
 * @property timestamp System timestamp in milliseconds when detected.
 */
data class DetectionResult(
    val targetPackage: String,
    val featureType: String,
    val matchedContainerId: String,
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

package com.feedshield.android.core.accessibility.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Strategy interface for detecting short-video layouts in target applications.
 * Cleanly decouples app-specific detection logic from the core AccessibilityService.
 */
interface ShortVideoDetector {

    /**
     * Target application package name (e.g. "com.instagram.android").
     */
    val targetPackage: String

    /**
     * Determines whether this detector can handle the given event.
     */
    fun canHandle(event: AccessibilityEvent): Boolean {
        val eventPkg = event.packageName?.toString() ?: return false
        return eventPkg == targetPackage
    }

    /**
     * Analyzes the accessibility node tree to determine if short-form video UI containers are active.
     *
     * @param rootNode Root AccessibilityNodeInfo of the active window.
     * @param event The triggered accessibility event.
     * @return [DetectionResult] if a short-form video container is active, or null if normal feed/messaging.
     */
    fun detect(rootNode: AccessibilityNodeInfo?, event: AccessibilityEvent): DetectionResult?
}

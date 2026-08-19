package com.feedshield.android.core.accessibility.interceptor

import android.accessibilityservice.AccessibilityService
import com.feedshield.android.core.accessibility.detector.DetectionResult

/**
 * Interface defining interception actions when a short-video UI layout is detected.
 * Enables modular addition of blocker overlays, cooldown timers, or gesture interventions.
 */
interface ShortVideoInterceptor {

    /**
     * Called when a short-form video UI is detected.
     *
     * @param service The active AccessibilityService instance (can trigger global actions if required).
     * @param result Metadata regarding the detection event.
     */
    fun onShortVideoDetected(service: AccessibilityService, result: DetectionResult)
}

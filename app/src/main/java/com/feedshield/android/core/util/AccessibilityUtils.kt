package com.feedshield.android.core.util

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * Utility helpers to verify accessibility status and navigate to system settings.
 */
object AccessibilityUtils {

    /**
     * Checks whether the given [serviceClass] is currently enabled in system accessibility settings.
     */
    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<out AccessibilityService>
    ): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false

        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )

        val expectedComponent = "${context.packageName}/${serviceClass.name}"
        for (service in enabledServices) {
            val id = service.id
            if (id.equals(expectedComponent, ignoreCase = true) || id.contains(serviceClass.simpleName)) {
                return true
            }
        }
        return false
    }

    /**
     * Creates an Intent to open the Android Accessibility Settings screen.
     */
    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}

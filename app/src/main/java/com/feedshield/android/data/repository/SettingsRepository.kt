package com.feedshield.android.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * On-device repository managing user preferences and onboarding state.
 * Guaranteed 100% on-device storage using Android SharedPreferences.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "feedshield_preferences"
        private const val KEY_DISCLOSURE_ACKNOWLEDGED = "key_disclosure_acknowledged"
        private const val KEY_INSTAGRAM_BLOCKED = "key_instagram_blocked"
        private const val KEY_YOUTUBE_BLOCKED = "key_youtube_blocked"
    }

    var isDisclosureAcknowledged: Boolean
        get() = prefs.getBoolean(KEY_DISCLOSURE_ACKNOWLEDGED, false)
        set(value) = prefs.edit().putBoolean(KEY_DISCLOSURE_ACKNOWLEDGED, value).apply()

    var isInstagramBlocked: Boolean
        get() = prefs.getBoolean(KEY_INSTAGRAM_BLOCKED, true)
        set(value) = prefs.edit().putBoolean(KEY_INSTAGRAM_BLOCKED, value).apply()

    var isYouTubeBlocked: Boolean
        get() = prefs.getBoolean(KEY_YOUTUBE_BLOCKED, true)
        set(value) = prefs.edit().putBoolean(KEY_YOUTUBE_BLOCKED, value).apply()
}

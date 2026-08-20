package com.feedshield.android.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * On-device repository managing user preferences, feature toggles, and snooze state.
 * Guaranteed 100% on-device storage using Android SharedPreferences.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "scroll_stopper_preferences"
        private const val KEY_DISCLOSURE_ACKNOWLEDGED = "key_disclosure_acknowledged"
        private const val KEY_INSTAGRAM_BLOCKED = "key_instagram_blocked"
        private const val KEY_YOUTUBE_BLOCKED = "key_youtube_blocked"
        private const val KEY_ALLOW_SINGLE_DM_REELS = "key_allow_single_dm_reels"
        private const val KEY_SNOOZE_UNTIL_TIMESTAMP = "key_snooze_until_timestamp"
        private const val KEY_OVERLAY_NOTICE_ENABLED = "key_overlay_notice_enabled"

        const val DEFAULT_SNOOZE_MINUTES = 15
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

    /**
     * When true, allows a user to view the initial Reel opened from DMs/links,
     * but blocks subsequent swipe-down gestures to prevent doom-scrolling.
     */
    var isAllowSingleDmReels: Boolean
        get() = prefs.getBoolean(KEY_ALLOW_SINGLE_DM_REELS, true)
        set(value) = prefs.edit().putBoolean(KEY_ALLOW_SINGLE_DM_REELS, value).apply()

    var isOverlayNoticeEnabled: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_NOTICE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_NOTICE_ENABLED, value).apply()

    var snoozeUntilTimestamp: Long
        get() = prefs.getLong(KEY_SNOOZE_UNTIL_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_SNOOZE_UNTIL_TIMESTAMP, value).apply()

    /**
     * Returns true if protection is currently paused due to an active snooze timer.
     */
    fun isSnoozed(): Boolean {
        return System.currentTimeMillis() < snoozeUntilTimestamp
    }

    /**
     * Returns remaining snooze duration in seconds, or 0 if inactive.
     */
    fun getRemainingSnoozeSeconds(): Long {
        val remaining = (snoozeUntilTimestamp - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0L
    }

    /**
     * Pauses protection for the specified number of minutes.
     */
    fun snoozeForMinutes(minutes: Int = DEFAULT_SNOOZE_MINUTES) {
        val until = System.currentTimeMillis() + (minutes * 60 * 1000L)
        snoozeUntilTimestamp = until
    }

    /**
     * Resumes protection immediately.
     */
    fun cancelSnooze() {
        snoozeUntilTimestamp = 0L
    }
}

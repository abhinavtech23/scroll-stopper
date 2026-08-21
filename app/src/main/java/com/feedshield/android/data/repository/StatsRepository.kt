package com.feedshield.android.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.feedshield.android.core.accessibility.detector.InstagramReelsDetector
import com.feedshield.android.core.accessibility.detector.InterceptionReason
import com.feedshield.android.core.accessibility.detector.YouTubeShortsDetector
import com.feedshield.android.data.model.DailyStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 100% on-device repository managing wellbeing statistics, daily interception history,
 * streak calculations, and time-saved estimations.
 */
class StatsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "scroll_stopper_stats"
        private const val PREFIX_TOTAL_BLOCKS = "blocks_total_"
        private const val PREFIX_INSTAGRAM_BLOCKS = "blocks_ig_"
        private const val PREFIX_YOUTUBE_BLOCKS = "blocks_yt_"
        private const val KEY_TOTAL_LIFETIME_BLOCKS = "lifetime_total_blocks"
    }

    /**
     * Records a new short-video interception event for the current day.
     */
    @Synchronized
    fun recordInterception(targetPackage: String, reason: InterceptionReason) {
        val today = DailyStats.todayDateStr()
        val isInstagram = targetPackage == InstagramReelsDetector.INSTAGRAM_PACKAGE
        val isYouTube = targetPackage == YouTubeShortsDetector.YOUTUBE_PACKAGE

        val totalKey = "$PREFIX_TOTAL_BLOCKS$today"
        val igKey = "$PREFIX_INSTAGRAM_BLOCKS$today"
        val ytKey = "$PREFIX_YOUTUBE_BLOCKS$today"

        val currentTotal = prefs.getInt(totalKey, 0)
        val currentIg = prefs.getInt(igKey, 0)
        val currentYt = prefs.getInt(ytKey, 0)
        val lifetimeTotal = prefs.getInt(KEY_TOTAL_LIFETIME_BLOCKS, 0)

        prefs.edit().apply {
            putInt(totalKey, currentTotal + 1)
            if (isInstagram) putInt(igKey, currentIg + 1)
            if (isYouTube) putInt(ytKey, currentYt + 1)
            putInt(KEY_TOTAL_LIFETIME_BLOCKS, lifetimeTotal + 1)
        }.apply()
    }

    /**
     * Retrieves today's wellbeing statistics.
     */
    fun getTodayStats(): DailyStats {
        val today = DailyStats.todayDateStr()
        val total = prefs.getInt("$PREFIX_TOTAL_BLOCKS$today", 0)
        val ig = prefs.getInt("$PREFIX_INSTAGRAM_BLOCKS$today", 0)
        val yt = prefs.getInt("$PREFIX_YOUTUBE_BLOCKS$today", 0)

        return DailyStats(
            dateStr = today,
            totalBlocks = total,
            instagramBlocks = ig,
            youtubeBlocks = yt
        )
    }

    /**
     * Retrieves the last 7 days of daily statistics in chronological order (earliest to today).
     */
    fun getLast7DaysStats(): List<DailyStats> {
        val statsList = mutableListOf<DailyStats>()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Collect the past 7 days (6 days ago -> today)
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = sdf.format(cal.time)
            val total = prefs.getInt("$PREFIX_TOTAL_BLOCKS$dateStr", 0)
            val ig = prefs.getInt("$PREFIX_INSTAGRAM_BLOCKS$dateStr", 0)
            val yt = prefs.getInt("$PREFIX_YOUTUBE_BLOCKS$dateStr", 0)

            statsList.add(
                DailyStats(
                    dateStr = dateStr,
                    dayLabel = DailyStats.formatDayLabel(dateStr),
                    totalBlocks = total,
                    instagramBlocks = ig,
                    youtubeBlocks = yt
                )
            )
        }

        return statsList
    }

    /**
     * Calculates the current consecutive daily streak (days with at least 1 interception).
     */
    fun calculateCurrentStreak(): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var streak = 0

        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)
        val todayBlocks = prefs.getInt("$PREFIX_TOTAL_BLOCKS$todayStr", 0)

        // If today has blocks, start streak counting from today; otherwise start from yesterday
        var checkIndex = 0
        if (todayBlocks == 0) {
            checkIndex = 1
        }

        while (true) {
            val checkCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -checkIndex)
            }
            val checkDateStr = sdf.format(checkCal.time)
            val blocks = prefs.getInt("$PREFIX_TOTAL_BLOCKS$checkDateStr", 0)

            if (blocks > 0) {
                streak++
                checkIndex++
            } else {
                break
            }
        }

        // Return at least 1 day streak if today has activity or 0
        return if (streak == 0 && todayBlocks > 0) 1 else streak
    }

    /**
     * Calculates total lifetime minutes saved across all sessions.
     */
    fun getTotalLifetimeMinutesSaved(): Int {
        val lifetimeBlocks = prefs.getInt(KEY_TOTAL_LIFETIME_BLOCKS, 0)
        return lifetimeBlocks * DailyStats.MINUTES_SAVED_PER_BLOCK
    }

    /**
     * Returns total lifetime blocks.
     */
    fun getLifetimeBlocksCount(): Int {
        return prefs.getInt(KEY_TOTAL_LIFETIME_BLOCKS, 0)
    }

    /**
     * Clears all local statistics.
     */
    fun resetAllStats() {
        prefs.edit().clear().apply()
    }
}

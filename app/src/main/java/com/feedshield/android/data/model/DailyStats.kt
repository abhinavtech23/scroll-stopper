package com.feedshield.android.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Encapsulates daily wellbeing statistics and interception totals.
 *
 * @property dateStr Formatted date (yyyy-MM-dd).
 * @property dayLabel Short day of week label (e.g. "Mon", "Tue").
 * @property totalBlocks Total short-video scroll attempts intercepted on this day.
 * @property instagramBlocks Interceptions occurring in Instagram Reels.
 * @property youtubeBlocks Interceptions occurring in YouTube Shorts.
 * @property minutesSaved Estimated minutes saved (calculated at ~3 mins per blocked doom-scroll session).
 */
data class DailyStats(
    val dateStr: String,
    val dayLabel: String = formatDayLabel(dateStr),
    val totalBlocks: Int = 0,
    val instagramBlocks: Int = 0,
    val youtubeBlocks: Int = 0,
    val minutesSaved: Int = totalBlocks * MINUTES_SAVED_PER_BLOCK
) {
    companion object {
        const val MINUTES_SAVED_PER_BLOCK = 3

        fun todayDateStr(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun formatDayLabel(dateStr: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateStr) ?: return dateStr
                val outSdf = SimpleDateFormat("EEE", Locale.getDefault())
                outSdf.format(date)
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}

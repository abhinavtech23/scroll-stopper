package com.feedshield.android.presentation.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.feedshield.android.data.model.DailyStats
import com.feedshield.android.data.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AnalyticsUiState(
    val todayStats: DailyStats = DailyStats(DailyStats.todayDateStr()),
    val weeklyStats: List<DailyStats> = emptyList(),
    val currentStreakDays: Int = 0,
    val lifetimeMinutesSaved: Int = 0,
    val lifetimeBlocksCount: Int = 0
)

class AnalyticsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val statsRepository = StatsRepository(application)

    private val _uiState = MutableStateFlow(loadStats())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun refreshStats() {
        _uiState.update { loadStats() }
    }

    private fun loadStats(): AnalyticsUiState {
        val today = statsRepository.getTodayStats()
        val weekly = statsRepository.getLast7DaysStats()
        val streak = statsRepository.calculateCurrentStreak()
        val lifetimeMinutes = statsRepository.getTotalLifetimeMinutesSaved()
        val lifetimeBlocks = statsRepository.getLifetimeBlocksCount()

        return AnalyticsUiState(
            todayStats = today,
            weeklyStats = weekly,
            currentStreakDays = streak,
            lifetimeMinutesSaved = lifetimeMinutes,
            lifetimeBlocksCount = lifetimeBlocks
        )
    }

    fun resetStats() {
        statsRepository.resetAllStats()
        refreshStats()
    }
}

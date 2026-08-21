package com.feedshield.android.core.accessibility.overlay

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.feedshield.android.R
import com.feedshield.android.core.accessibility.detector.DetectionResult
import com.feedshield.android.core.accessibility.detector.InterceptionReason
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository
import com.feedshield.android.data.repository.StatsRepository

/**
 * Manages the floating on-device native overlay HUD displayed upon short-video interception.
 * Features positive reinforcement feedback ("+3 mins saved! Great job staying focused").
 */
class ScrollStopperOverlayManager(
    private val service: AccessibilityService,
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository? = null
) {

    companion object {
        private const val TAG = "ScrollStopper.Overlay"
        private const val AUTO_DISMISS_DELAY_MS = 3800L
    }

    private val windowManager: WindowManager =
        service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentOverlayView: View? = null
    private var dismissRunnable: Runnable? = null

    /**
     * Displays a positive reinforcement interception notice overlay.
     */
    fun showInterceptionNotice(result: DetectionResult) {
        mainHandler.post {
            try {
                removeOverlayInternal()

                val overlayView = buildOverlayView(result)
                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    y = 120 // Margin from bottom in pixels
                }

                overlayView.alpha = 0f
                overlayView.translationY = 60f
                windowManager.addView(overlayView, layoutParams)
                currentOverlayView = overlayView

                // Smooth slide up & fade in
                overlayView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(280)
                    .setInterpolator(DecelerateInterpolator())
                    .start()

                // Auto-dismiss schedule
                val autoDismiss = Runnable {
                    dismissOverlay()
                }
                dismissRunnable = autoDismiss
                mainHandler.postDelayed(autoDismiss, AUTO_DISMISS_DELAY_MS)

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to present overlay window: ${e.message}", e)
            }
        }
    }

    private fun buildOverlayView(result: DetectionResult): View {
        val context = service
        val dp = context.resources.displayMetrics.density

        val todayStats = statsRepository?.getTodayStats()
        val todayMinutes = todayStats?.minutesSaved ?: 3

        // Root container with card background
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (18 * dp).toInt()
            setPadding(padding, padding, padding, padding)

            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#161E2E"))
                cornerRadius = 20 * dp
                setStroke((1.5f * dp).toInt(), Color.parseColor("#10B981")) // Positive Green Accent
            }
            background = shape
            layoutParams = LinearLayout.LayoutParams(
                (350 * dp).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        // Header Row (Positive Reinforcement Badge)
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleView = TextView(context).apply {
            text = "🛡️ Focus Shield Intercepted"
            textSize = 15f
            setTextColor(Color.parseColor("#F8FAFC"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        headerLayout.addView(titleView)
        rootLayout.addView(headerLayout)

        // Positive Reinforcement Highlight
        val positivePill = TextView(context).apply {
            text = "🎉 +3 mins saved! (~${todayMinutes}m saved today)"
            textSize = 12f
            setTextColor(Color.parseColor("#10B981")) // Green
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val pillShape = GradientDrawable().apply {
                setColor(Color.parseColor("#064E3B"))
                cornerRadius = 8 * dp
            }
            background = pillShape
            val hp = (8 * dp).toInt()
            val vp = (4 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (6 * dp).toInt()
                bottomMargin = (4 * dp).toInt()
            }
            layoutParams = params
        }
        rootLayout.addView(positivePill)

        // Description Message
        val messageText = when (result.reason) {
            InterceptionReason.INSTAGRAM_REELS_SWIPE_PREVENTED ->
                "Single Reel viewed. Further scrolling blocked to keep your focus sharp!"
            InterceptionReason.INSTAGRAM_REELS_IMMEDIATE ->
                "Instagram Reels blocked. DMs and normal feeds are still active!"
            InterceptionReason.YOUTUBE_SHORTS ->
                "YouTube Shorts blocked. Back to intentional watching!"
        }

        val descView = TextView(context).apply {
            text = messageText
            textSize = 13f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, (4 * dp).toInt(), 0, (12 * dp).toInt())
        }
        rootLayout.addView(descView)

        // Button Row
        val buttonRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        // Snooze Button
        val snoozeButton = Button(context).apply {
            text = context.getString(R.string.btn_snooze_15m)
            textSize = 12f
            setTextColor(Color.parseColor("#06B6D4"))
            val btnShape = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                cornerRadius = 10 * dp
            }
            background = btnShape
            setPadding((12 * dp).toInt(), (4 * dp).toInt(), (12 * dp).toInt(), (4 * dp).toInt())
            setOnClickListener {
                settingsRepository.snoozeForMinutes(15)
                Logger.i(TAG, "Protection snoozed for 15 minutes via overlay.")
                dismissOverlay()
            }
        }
        buttonRow.addView(snoozeButton)

        // Dismiss Button
        val dismissButton = Button(context).apply {
            text = context.getString(R.string.btn_dismiss)
            textSize = 12f
            setTextColor(Color.parseColor("#F8FAFC"))
            val btnShape = GradientDrawable().apply {
                setColor(Color.parseColor("#2563EB"))
                cornerRadius = 10 * dp
            }
            background = btnShape
            setPadding((14 * dp).toInt(), (4 * dp).toInt(), (14 * dp).toInt(), (4 * dp).toInt())
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = (8 * dp).toInt()
            }
            layoutParams = params
            setOnClickListener {
                dismissOverlay()
            }
        }
        buttonRow.addView(dismissButton)

        rootLayout.addView(buttonRow)
        return rootLayout
    }

    fun dismissOverlay() {
        mainHandler.post {
            val view = currentOverlayView ?: return@post
            view.animate()
                .alpha(0f)
                .translationY(40f)
                .setDuration(200)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    removeOverlayInternal()
                }
                .start()
        }
    }

    private fun removeOverlayInternal() {
        dismissRunnable?.let { mainHandler.removeCallbacks(it) }
        dismissRunnable = null
        currentOverlayView?.let { view ->
            try {
                windowManager.removeViewImmediate(view)
            } catch (e: Exception) {
                Logger.w(TAG, "Error removing overlay view: ${e.message}")
            }
        }
        currentOverlayView = null
    }

    fun destroy() {
        removeOverlayInternal()
    }
}

package com.feedshield.android.core.accessibility.overlay

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.feedshield.android.core.accessibility.detector.DetectionResult
import com.feedshield.android.core.util.Logger
import com.feedshield.android.data.repository.SettingsRepository
import com.feedshield.android.data.repository.StatsRepository

/**
 * Manages the full-screen blur & motivational focus overlay displayed upon short-video interception.
 * Instead of jarring automatic back-navigation, blurs the reel/short video and delivers a powerful,
 * respectful motivational intervention directly on screen.
 */
class ScrollStopperOverlayManager(
    private val service: AccessibilityService,
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository? = null
) {

    companion object {
        private const val TAG = "ScrollStopper.Overlay"
        private const val AUTO_DISMISS_DELAY_MS = 9000L
    }

    private val windowManager: WindowManager =
        service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentOverlayView: View? = null
    private var dismissRunnable: Runnable? = null

    /**
     * Displays the full-screen blur overlay with the motivational message.
     */
    fun showInterceptionNotice(result: DetectionResult) {
        mainHandler.post {
            try {
                // If overlay is already active, do not flicker or duplicate
                if (currentOverlayView != null) {
                    Logger.d(TAG, "Blur overlay is already displayed. Keeping current view.")
                    return@post
                }

                val overlayView = buildOverlayView(result)
                var flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                }

                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    flags,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        blurBehindRadius = 80
                    }
                }

                overlayView.alpha = 0f
                windowManager.addView(overlayView, layoutParams)
                currentOverlayView = overlayView

                // Smooth fade in
                overlayView.animate()
                    .alpha(1f)
                    .setDuration(240)
                    .setInterpolator(DecelerateInterpolator())
                    .start()

                // Auto-dismiss schedule
                val autoDismiss = Runnable {
                    dismissOverlay()
                }
                dismissRunnable = autoDismiss
                mainHandler.postDelayed(autoDismiss, AUTO_DISMISS_DELAY_MS)

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to present blur overlay window: ${e.message}", e)
            }
        }
    }

    private fun buildOverlayView(result: DetectionResult): View {
        val context = service
        val dp = context.resources.displayMetrics.density

        val todayStats = statsRepository?.getTodayStats()
        val todayMinutes = todayStats?.minutesSaved ?: 3

        // Fullscreen Root Frame: Translucent dark blur scrim (obscures the reel video)
        val rootLayout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            // Heavy translucent dark frosted backdrop: 92% opacity dark glass
            setBackgroundColor(Color.argb(235, 8, 8, 8))
            isClickable = true
            isFocusable = true
        }

        // Center Motivational Card
        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val padH = (24 * dp).toInt()
            val padV = (28 * dp).toInt()
            setPadding(padH, padV, padH, padV)

            val cardShape = GradientDrawable().apply {
                setColor(Color.parseColor("#121212")) // Premium rich black
                cornerRadius = 24 * dp
                setStroke((1.5f * dp).toInt(), Color.parseColor("#2E2E2E")) // Subtle border
            }
            background = cardShape

            // Center in parent FrameLayout with margins
            val lp = FrameLayout.LayoutParams(
                (340 * dp).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                val margin = (24 * dp).toInt()
                setMargins(margin, margin, margin, margin)
            }
            layoutParams = lp
        }

        // 1. Tag / Badge (SCROLL STOPPER)
        val badge = TextView(context).apply {
            text = "SCROLL STOPPER"
            textSize = 11f
            setTextColor(Color.parseColor("#888888"))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.15f
            val badgeShape = GradientDrawable().apply {
                setColor(Color.parseColor("#1C1C1C"))
                cornerRadius = 6 * dp
                setStroke((1 * dp).toInt(), Color.parseColor("#333333"))
            }
            background = badgeShape
            val hp = (10 * dp).toInt()
            val vp = (4 * dp).toInt()
            setPadding(hp, vp, hp, vp)
        }
        cardLayout.addView(badge)

        // 2. Headline: "STOP SCROLLING MY BROTHER"
        val headline = TextView(context).apply {
            text = "STOP SCROLLING MY BROTHER"
            textSize = 19f
            setTextColor(Color.parseColor("#FFFFFF"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (16 * dp).toInt()
            }
            layoutParams = lp
        }
        cardLayout.addView(headline)

        // 3. Motivational Body
        val bodyText = TextView(context).apply {
            text = "YOU ARE MUCH BIGGER THAN THIS\nYOU HAVE GREAT POTENTIAL\nDONT WASTE YOU POTENTIAL"
            textSize = 14f
            setTextColor(Color.parseColor("#D1D5DB"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setLineSpacing(0f, 1.35f)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (12 * dp).toInt()
            }
            layoutParams = lp
        }
        cardLayout.addView(bodyText)

        // 4. Signature: "- SCROLL STOPPER TEAM"
        val signature = TextView(context).apply {
            text = "— SCROLL STOPPER TEAM"
            textSize = 12f
            setTextColor(Color.parseColor("#9CA3AF"))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.1f
            gravity = Gravity.CENTER
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (14 * dp).toInt()
                bottomMargin = (14 * dp).toInt()
            }
            layoutParams = lp
        }
        cardLayout.addView(signature)

        // 5. Stat Pill: (+3m saved • ~Xm saved today)
        val statPill = TextView(context).apply {
            text = "+3 mins saved • ~${todayMinutes}m saved today"
            textSize = 11f
            setTextColor(Color.parseColor("#888888"))
            gravity = Gravity.CENTER
            val pillShape = GradientDrawable().apply {
                setColor(Color.parseColor("#181818"))
                cornerRadius = 12 * dp
            }
            background = pillShape
            val hp = (12 * dp).toInt()
            val vp = (4 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (20 * dp).toInt()
            }
            layoutParams = lp
        }
        cardLayout.addView(statPill)

        // 6. Action Buttons Row: [ GO BACK ] and [ I'LL FOCUS ]
        val buttonRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = lp
        }

        // Button: GO BACK (leaves the reel screen manually)
        val goBackButton = Button(context).apply {
            text = "GO BACK"
            textSize = 13f
            setTextColor(Color.parseColor("#000000"))
            typeface = Typeface.DEFAULT_BOLD
            val btnShape = GradientDrawable().apply {
                setColor(Color.parseColor("#FFFFFF")) // Pure White
                cornerRadius = 12 * dp
            }
            background = btnShape
            val hp = (16 * dp).toInt()
            val vp = (8 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            val lp = LinearLayout.LayoutParams(0, (46 * dp).toInt(), 1f).apply {
                rightMargin = (6 * dp).toInt()
            }
            layoutParams = lp
            setOnClickListener {
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                dismissOverlay()
            }
        }
        buttonRow.addView(goBackButton)

        // Button: I'LL FOCUS (dismisses overlay)
        val focusButton = Button(context).apply {
            text = "I'LL FOCUS"
            textSize = 13f
            setTextColor(Color.parseColor("#FFFFFF"))
            typeface = Typeface.DEFAULT_BOLD
            val btnShape = GradientDrawable().apply {
                setColor(Color.parseColor("#222222")) // Dark elevated
                cornerRadius = 12 * dp
                setStroke((1 * dp).toInt(), Color.parseColor("#383838"))
            }
            background = btnShape
            val hp = (16 * dp).toInt()
            val vp = (8 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            val lp = LinearLayout.LayoutParams(0, (46 * dp).toInt(), 1f).apply {
                leftMargin = (6 * dp).toInt()
            }
            layoutParams = lp
            setOnClickListener {
                dismissOverlay()
            }
        }
        buttonRow.addView(focusButton)

        cardLayout.addView(buttonRow)

        // 7. Snooze link at bottom
        val snoozeLink = TextView(context).apply {
            text = "Snooze protection for 15 minutes"
            textSize = 11f
            setTextColor(Color.parseColor("#666666"))
            gravity = Gravity.CENTER
            val hp = (10 * dp).toInt()
            val vp = (8 * dp).toInt()
            setPadding(hp, vp, hp, vp)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (12 * dp).toInt()
            }
            layoutParams = lp
            setOnClickListener {
                settingsRepository.snoozeForMinutes(15)
                Logger.i(TAG, "Protection snoozed for 15 minutes via blur overlay.")
                dismissOverlay()
            }
        }
        cardLayout.addView(snoozeLink)

        rootLayout.addView(cardLayout)
        return rootLayout
    }

    fun dismissOverlay() {
        mainHandler.post {
            val view = currentOverlayView ?: return@post
            view.animate()
                .alpha(0f)
                .setDuration(180)
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

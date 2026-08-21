# Scroll Stopper 🛡️

> **Stop the scroll. Reclaim your time.**

**Scroll Stopper** is a privacy-first, 100% on-device Android app that detects and blocks addictive short-form video doom-scrolling on **Instagram Reels** and **YouTube Shorts** — while keeping your normal feeds, DMs, search, and regular videos fully functional.

No cloud. No tracking. No data leaves your phone. Ever.

---

## ✨ Features

### 🔒 Core Protection Engine
- **Instagram Reels Interception** — Detects Reels UI containers in real-time and performs a `GLOBAL_ACTION_BACK` to exit the Reels player instantly.
- **YouTube Shorts Blocker** — Identifies the immersive Shorts player and immediately redirects users back to intentional content.
- **Smart DM Reel Exception** — When someone sends you a Reel via Direct Message, you can watch that one Reel. After a 1.8-second loading grace period, any further vertical swipe attempts are intercepted. You see the reel, not the rabbit hole.
- **Event Throttling** — `TYPE_WINDOW_CONTENT_CHANGED` events are throttled to 150ms intervals, eliminating Instagram tap delay and like-button lag.

### 🎯 Floating HUD Overlay
- **Interception Notice** — A beautiful floating notification card appears on-screen when a scroll is blocked, with a positive reinforcement message.
- **15-Minute Snooze Mode** — One-tap snooze button pauses protection for 15 minutes with a live countdown timer displayed in the app.
- **Auto-Dismiss** — The overlay auto-dismisses after a few seconds so it never blocks your screen.

### 📊 Wellbeing Dashboard
- **Today's Stats** — Live counters showing "Scrolls Intercepted Today", "Estimated Time Saved", and "Current Streak".
- **7-Day Activity Chart** — Animated bar chart visualizing your blocked attempts over the past week with today highlighted.
- **Platform Breakdown** — See your Instagram Reels vs YouTube Shorts interception ratio.
- **Time Saved Calculator** — Estimates ~3 minutes saved per interception, with cumulative daily and lifetime totals.

### 🏆 Gamification & Milestones
| Badge | Requirement |
|-------|------------|
| 🌟 Focus Starter | First interception |
| 🔥 3-Day Focus Streak | 3 consecutive days with at least 1 block |
| ⏱️ Time Master | 1+ hour of total time saved |

### ⚙️ Rules & Configuration
- **Per-App Toggles** — Enable/disable protection individually for Instagram and YouTube.
- **DM Reel Exception Toggle** — Turn the single-DM-reel viewing feature on or off.
- **Snooze Controls** — Quick 15-minute protection pause from Dashboard or overlay.

### 🔐 Privacy & Data Safety (Google Play Compliant)
- **Zero Network Permissions** — The app has NO internet permission. Zero networking calls exist in the codebase.
- **No Third-Party SDKs** — No analytics, crash reporting, or ad SDKs.
- **100% On-Device Processing** — All data stays in local SharedPreferences.
- **Data Reset** — One-tap "Wipe All Data" button in the Privacy tab.
- **Mandatory Accessibility Disclosure** — Clear, prominent in-app disclosure explaining exactly how and why Accessibility APIs are used.

---

## 📱 Screenshots

The app has 4 main tabs via bottom navigation:

| Dashboard | Analytics | Rules | Privacy |
|-----------|-----------|-------|---------|
| Hero shield status, live snooze countdown, quick stats | 7-day bar chart, stat cards, milestones | Per-app toggles, DM exception config | Data safety info, accessibility guide, data wipe |

---

## 🏗️ Architecture

Built with **Native Android (Kotlin)** using **Jetpack Compose** and **Material 3** design system, following **Clean Architecture** with **MVVM** pattern.

```
com.feedshield.android/
│
├── FeedShieldApp.kt                          # Application class
├── MainActivity.kt                           # Entry point → MainNavigationScreen
│
├── core/
│   ├── accessibility/
│   │   ├── FeedShieldAccessibilityService.kt # Core engine & event dispatcher
│   │   ├── NodeInspector.kt                  # Memory-safe UI tree crawler (depth-bounded)
│   │   │
│   │   ├── detector/
│   │   │   ├── ShortVideoDetector.kt         # Strategy interface
│   │   │   ├── DetectionResult.kt            # Result model with InterceptionReason
│   │   │   ├── InstagramReelsDetector.kt     # 3-state Reel session tracker + DM exception
│   │   │   └── YouTubeShortsDetector.kt      # Shorts container detection
│   │   │
│   │   ├── interceptor/
│   │   │   ├── ShortVideoInterceptor.kt      # Interceptor interface
│   │   │   └── DefaultShortVideoInterceptor.kt # BACK action + stats + overlay trigger
│   │   │
│   │   └── overlay/
│   │       └── ScrollStopperOverlayManager.kt # WindowManager floating HUD
│   │
│   └── util/
│       ├── AccessibilityUtils.kt             # Settings navigation & status checks
│       └── Logger.kt                         # Debug logging utility
│
├── data/
│   ├── model/
│   │   └── DailyStats.kt                    # Daily blocks & time saved data class
│   └── repository/
│       ├── SettingsRepository.kt             # SharedPreferences: toggles, snooze state
│       └── StatsRepository.kt               # Daily history, streaks, lifetime totals
│
└── presentation/
    ├── main/
    │   └── MainNavigationScreen.kt           # 4-tab bottom navigation scaffold
    ├── dashboard/
    │   └── DashboardScreen.kt                # Hero shield, snooze timer, quick stats
    ├── analytics/
    │   ├── AnalyticsScreen.kt                # Stats, chart, breakdown, milestones
    │   └── components/
    │       ├── WeeklyBarChart.kt             # Animated 7-day bar chart (Canvas)
    │       ├── StatCard.kt                   # Reusable stat display card
    │       └── AppBreakdownCard.kt           # Instagram vs YouTube ratio card
    ├── rules/
    │   └── RulesScreen.kt                    # Per-app protection toggles
    ├── privacy/
    │   └── PrivacyScreen.kt                  # Data safety, setup guide, wipe button
    ├── onboarding/
    │   ├── OnboardingScreen.kt               # First-launch setup & disclosure
    │   ├── OnboardingViewModel.kt            # MVVM ViewModel with live ticker
    │   └── components/
    │       ├── DisclosureCard.kt             # Required privacy disclosure UI
    │       └── ServiceStatusIndicator.kt     # Active/inactive status indicator
    └── theme/
        ├── Color.kt                          # App color palette
        ├── Theme.kt                          # Material 3 theme definition
        └── Type.kt                           # Typography scale
```

---

## 📋 Requirements

### Development Machine
| Requirement | Details |
|-------------|---------|
| **OS** | Windows 10/11, macOS, or Linux |
| **Android Studio** | Arctic Fox or newer (with bundled JBR) |
| **Java** | JDK 17 (bundled with Android Studio) |
| **Gradle** | 8.13 (auto-downloaded via Gradle Wrapper) |
| **Android SDK** | API 34 (Android 14) |

### Target Device
| Requirement | Details |
|-------------|---------|
| **Minimum Android** | Android 8.0 (API 26) |
| **Target Android** | Android 14 (API 34) |
| **Permissions Required** | Accessibility Service only |
| **Internet Required** | ❌ No — fully offline |
| **Storage** | ~5 MB APK |

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/scroll-stopper.git
cd scroll-stopper
```

### 2. Connect Your Android Phone

- Enable **Developer Options** on your phone (tap Build Number 7 times in Settings → About Phone)
- Enable **USB Debugging** in Developer Options
- Connect phone via USB cable
- Accept the USB debugging prompt on your phone

### 3. Build & Run

**One command — just like `flutter run`:**

```bash
.\run.bat
```

That's it! The script will:
1. ✅ Compile the debug APK
2. ✅ Install it on your connected phone
3. ✅ Launch the app automatically

### 4. Enable the Accessibility Service

After the app launches on your phone:
1. Tap **"Enable in Accessibility Settings"**
2. Find **"Scroll Stopper Protection Engine"** in the list
3. Toggle it **ON**
4. Confirm the permission dialog
5. Return to the app — you'll see **"Protection Active & Running"** ✅

---

## 🛠️ Manual Build Commands

If you prefer running commands manually:

```powershell
# Build debug APK
cmd /c "set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr&& gradle assembleDebug"

# Install on phone
adb install -r "app/build/outputs/apk/debug/app-debug.apk"

# Launch the app
adb shell am start -n com.feedshield.android/.MainActivity
```

---

## 🧩 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin 1.9.x |
| **UI Framework** | Jetpack Compose with Material 3 |
| **Architecture** | Clean Architecture + MVVM |
| **Local Storage** | SharedPreferences (zero database) |
| **Graphics** | Canvas API for animated charts |
| **Core Engine** | Android AccessibilityService API |
| **Overlay** | WindowManager system overlay |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 14 (API 34) |
| **Dependencies** | AndroidX Core, Lifecycle, Compose BOM, Material Icons Extended, Coroutines |

---

## 🔒 Accessibility API Disclosure

> **"Scroll Stopper uses Accessibility APIs strictly to detect short-video UI layouts on Instagram and YouTube. No personal data, messages, or text inputs are recorded, stored, or transmitted. All processing happens 100% on-device."**

### What the service accesses:
- ✅ UI view IDs and class names (to detect Reels/Shorts containers)
- ✅ Window state changes (to know when apps open/close)

### What the service does NOT access:
- ❌ Text content or messages
- ❌ Passwords or form inputs
- ❌ Photos, videos, or media files
- ❌ Notification content
- ❌ Any data outside Instagram and YouTube

---

## 📄 How It Works (Technical)

1. **AccessibilityService** monitors `TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOW_CONTENT_CHANGED` events from Instagram and YouTube only.
2. **InstagramReelsDetector** checks for Reel-specific view IDs (`clips_viewer_view_pager`, `reel_recycler_view`) and tracks a 3-state session: `IDLE → FIRST_REEL_LOADING → FIRST_REEL_READY → SCROLLING_BLOCKED`.
3. **YouTubeShortsDetector** checks for Shorts player container IDs and triggers immediate exit.
4. **DefaultShortVideoInterceptor** performs `GLOBAL_ACTION_BACK`, records the block in `StatsRepository`, and shows the overlay HUD.
5. **ScrollStopperOverlayManager** renders a floating `WindowManager` view with the interception message and snooze button.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/tiktok-support`)
3. Make your changes
4. Test on a real device (accessibility services need a real phone)
5. Submit a Pull Request

---

## 📜 License

This project is open source. See [LICENSE](LICENSE) for details.

---

## 🗺️ Roadmap

- [ ] TikTok support
- [ ] Reddit Shorts/Video support
- [ ] Custom cooldown timer (5/10/15/30 min snooze options)
- [ ] Daily screen time limit integration
- [ ] Widget for quick toggle from home screen
- [ ] Google Play Store release
- [ ] Custom app icon and branding

---

<p align="center">
  <b>Built with ❤️ to help people reclaim their focus and time.</b>
</p>

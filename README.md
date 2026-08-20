# Scroll Stopper 🛡️

**Scroll Stopper** is a privacy-first, 100% on-device open-source Digital Wellbeing Android application designed to detect and intercept addictive short-form video doom-scrolling (**Instagram Reels** and **YouTube Shorts**) while keeping standard feeds, direct messages, searches, and regular video playback fully functional.

---

## 🌟 Key Features

- **Focused Interception**: Detects Reels & Shorts UI view hierarchies in real time without interfering with regular posts or chats.
- **Single DM Reel Exception**: Allows users to watch a single Reel received via Direct Message or link, but intercepts subsequent vertical swipe-down gestures.
- **YouTube Shorts Blocker**: Immediately exits the immersive Shorts player and redirects users to intentional content.
- **Floating HUD Notice**: Displays an elegant, auto-dismissing on-screen notification card upon interception with quick Snooze options.
- **15-Minute Snooze Mode**: Easily pause protection for 15 minutes when needed with a live countdown ticker.
- **Zero-Cloud & 100% On-Device**: Does not require internet access permissions or cloud telemetry.
- **Strict Privacy Compliance**: Explicit prominent disclosure adhering to Google Play Accessibility API requirements.

---

## 🏗️ Architecture

```
com.feedshield.android/
├── core/
│   ├── accessibility/
│   │   ├── FeedShieldAccessibilityService.kt  # Core Accessibility Engine & Event Dispatcher
│   │   ├── NodeInspector.kt                  # Memory-safe UI Tree Inspector & Crawler
│   │   ├── detector/
│   │   │   ├── ShortVideoDetector.kt         # Detector Strategy Interface
│   │   │   ├── DetectionResult.kt            # Model with InterceptionReason & metadata
│   │   │   ├── InstagramReelsDetector.kt     # Reels detection & single DM swipe tracking
│   │   │   └── YouTubeShortsDetector.kt      # Shorts detection logic & ID signatures
│   │   ├── interceptor/
│   │   │   ├── ShortVideoInterceptor.kt      # Interceptor Interface
│   │   │   └── DefaultShortVideoInterceptor.kt # Back action + HUD dispatcher + Snooze checks
│   │   └── overlay/
│   │       └── ScrollStopperOverlayManager.kt # WindowManager native floating HUD view
│   └── util/
│       ├── AccessibilityUtils.kt             # Settings navigation & status checks
│       └── Logger.kt                         # Diagnostic logging
├── data/
│   └── repository/
│       └── SettingsRepository.kt             # Local SharedPreferences & Snooze manager
└── presentation/
    ├── onboarding/
    │   ├── OnboardingScreen.kt               # Jetpack Compose UI with Toggles & Snooze
    │   ├── OnboardingViewModel.kt            # MVVM ViewModel with live Ticker
    │   └── components/
    │       ├── DisclosureCard.kt             # Required Privacy & Policy Disclosure
    │       └── ServiceStatusIndicator.kt     # Active/Inactive status card
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 🔒 Mandatory Accessibility Disclosure

> **"Scroll Stopper uses Accessibility APIs strictly to detect short-video UI layouts on Instagram and YouTube. No personal data, messages, or text inputs are recorded, stored, or transmitted."**

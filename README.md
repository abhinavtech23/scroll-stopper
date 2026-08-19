# FeedShield 🛡️

**FeedShield** is a privacy-first, 100% on-device open-source Digital Wellbeing Android application designed to intercept addictive short-form video doom-scrolling (**Instagram Reels** and **YouTube Shorts**) while keeping standard feeds, direct messages, searches, and regular video playback fully functional.

---

## 🌟 Key Features

- **Focused Interception**: Detects Reels & Shorts UI view hierarchies in real time without interfering with regular posts or chats.
- **Zero-Cloud & 100% On-Device**: Does not use internet permissions or cloud telemetry.
- **Strict Privacy Compliance**: Explicit prominent disclosure adhering to Google Play Accessibility API requirements.
- **Clean Architecture & MVVM**: Modular Strategy-based layout detection (`ShortVideoDetector`, `NodeInspector`, `ShortVideoInterceptor`).

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
│   │   │   ├── DetectionResult.kt            # Model with confidence & view IDs
│   │   │   ├── InstagramReelsDetector.kt     # Reels detection logic & ID signatures
│   │   │   └── YouTubeShortsDetector.kt      # Shorts detection logic & ID signatures
│   │   └── interceptor/
│   │       ├── ShortVideoInterceptor.kt      # Interceptor Interface
│   │       └── DefaultShortVideoInterceptor.kt
│   └── util/
│       ├── AccessibilityUtils.kt             # Settings navigation & status checks
│       └── Logger.kt                         # Diagnostic logging
├── data/
│   └── repository/
│       └── SettingsRepository.kt             # Local SharedPreferences manager
└── presentation/
    ├── onboarding/
    │   ├── OnboardingScreen.kt               # Jetpack Compose UI
    │   ├── OnboardingViewModel.kt            # MVVM ViewModel
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

> **"FeedShield uses Accessibility APIs strictly to detect short-video UI layouts on Instagram and YouTube. No personal data, messages, or text inputs are recorded, stored, or transmitted."**

---

## 🚀 Getting Started

1. Open project in Android Studio (Giraffe / Hedgehog / Iguana or later).
2. Build and run on an Android device running Android 8.0 (API 26) or higher.
3. Complete the onboarding screen and tap **Enable in Accessibility Settings** to activate the shield.

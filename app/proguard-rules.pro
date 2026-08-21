# Scroll Stopper Production Proguard & R8 Rules

# Accessibility Service and Core Engine
-keep class com.feedshield.android.core.accessibility.** { *; }
-keep class com.feedshield.android.core.accessibility.detector.** { *; }
-keep class com.feedshield.android.core.accessibility.interceptor.** { *; }
-keep class com.feedshield.android.core.accessibility.overlay.** { *; }

# Data Models & Repositories
-keep class com.feedshield.android.data.model.** { *; }
-keep class com.feedshield.android.data.repository.** { *; }

# Jetpack Compose and ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Android Framework Callbacks
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Suppress harmless warnings for clean release builds
-dontwarn okio.**
-dontwarn javax.annotation.**

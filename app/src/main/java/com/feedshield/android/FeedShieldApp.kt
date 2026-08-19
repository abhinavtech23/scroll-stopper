package com.feedshield.android

import android.app.Application
import com.feedshield.android.core.util.Logger

/**
 * Application entry class for FeedShield.
 */
class FeedShieldApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.i("FeedShieldApp", "FeedShield initialized on-device.")
    }
}

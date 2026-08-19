package com.feedshield.android.core.util

import android.util.Log

/**
 * Lightweight, structured logger for FeedShield.
 * Prepares clean diagnostics for on-device detection and accessibility events.
 */
object Logger {
    private const val DEFAULT_TAG = "FeedShield"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, message)
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, message)
    }

    fun w(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}

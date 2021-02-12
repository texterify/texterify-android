package com.texterify.android.ota

import android.util.Log

/**
 * Central place for logging to control if and how much we log.
 *
 * Use `adb shell setprop log.tag.Txtfy VERBOSE` to enable logging for Texterify.
 */
internal object Logger {
    private const val TAG = "Txtfy"

    fun v(message: String) = filterLog(Log.VERBOSE) {
        Log.d(TAG, message)
    }

    fun d(message: String) = filterLog(Log.DEBUG) {
        Log.d(TAG, message)
    }

    fun i(message: String) = filterLog(Log.INFO) {
        Log.i(TAG, message)
    }

    fun w(message: String) = filterLog(Log.WARN) {
        Log.w(TAG, message)
    }

    fun w(message: String, error: Exception) = filterLog(Log.WARN) {
        Log.w(TAG, message, error)
    }

    fun e(message: String, error: Exception) = filterLog(Log.ERROR) {
        Log.e(TAG, message, error)
    }

    private inline fun filterLog(level: Int, crossinline log: () -> Unit) {
        if (Log.isLoggable(TAG, level)) {
            log()
        }
    }
}

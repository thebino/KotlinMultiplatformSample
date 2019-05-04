package com.example

import android.util.Log
import android.os.Build


actual fun initLogger() {
    Log.d(TAG, "====================================")
    Log.d(TAG, "Android|${Build.VERSION.RELEASE}")
    Log.d(TAG, "====================================")
}

const val TAG = "androidLib"

actual fun log(logLevel: LogLevel, message: String, throwable: Throwable?, vararg args: Any?) {
    when (logLevel) {
        LogLevel.DEBUG -> {
            Log.d(TAG, message, throwable)
        }
        LogLevel.INFO -> {
            Log.i(TAG, message, throwable)
        }
        LogLevel.WARN -> {
            Log.w(TAG, message, throwable)
        }
        LogLevel.ERROR -> {
            Log.e(TAG, message, throwable)
        }
    }
}

package com.example

import platform.UIKit.UIDevice


actual fun initLogger() {
    println("====================================")
    println("${UIDevice.currentDevice.systemName}|${UIDevice.currentDevice.systemVersion}")
    println("====================================")
}

actual fun log(logLevel: LogLevel, message: String, throwable: Throwable?, vararg args: Any?) {
    println("${logLevel.name} | $message")
}

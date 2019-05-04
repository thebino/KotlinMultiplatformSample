package com.example

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

expect fun initLogger()

expect fun log(logLevel: LogLevel, message: String, throwable: Throwable?, vararg args: Any?)

fun debug(message: String, throwable: Throwable? = null, vararg args: Any? = emptyArray()) {
    log(LogLevel.DEBUG, message, throwable, args)
}

fun info(message: String, throwable: Throwable? = null, vararg args: Any? = emptyArray()) {
    log(LogLevel.INFO, message, throwable, args)
}

fun warn(message: String, throwable: Throwable? = null, vararg args: Any? = emptyArray()) {
    log(LogLevel.WARN, message, throwable, args)
}

fun error(message: String, throwable: Throwable? = null, vararg args: Any? = emptyArray()) {
    log(LogLevel.ERROR, message, throwable, args)
}

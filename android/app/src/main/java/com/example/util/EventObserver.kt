package com.example.util

import androidx.lifecycle.Observer


/**
 * An [Observer] for [SingleTimeEvent]s, simplifying the pattern of checking if the [SingleTimeEvent]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [SingleTimeEvent]'s contents has not been handled.
 *
 * @link https://gist.github.com/JoseAlcerreca/e0bba240d9b3cffa258777f12e5c0ae9
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<SingleTimeEvent<T>> {
    override fun onChanged(event: SingleTimeEvent<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}

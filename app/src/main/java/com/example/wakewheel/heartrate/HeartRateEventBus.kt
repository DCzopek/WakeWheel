package com.example.wakewheel.heartrate

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class HeartRateEventBus {

    private val channel = BroadcastChannel<Int>(1)

    fun send(value: Int) {
        MainScope().launch {
            channel.send(value)
        }
    }

    fun listen(): BroadcastChannel<Int> =
        channel
}
package com.example.wakewheel.receivers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel

@ExperimentalCoroutinesApi
class HeartRateEventBus {

    private val channel = BroadcastChannel<String>(1)

    suspend fun send(value: String) {
        channel.send(value)
    }

    fun listen(): BroadcastChannel<String> =
        channel
}
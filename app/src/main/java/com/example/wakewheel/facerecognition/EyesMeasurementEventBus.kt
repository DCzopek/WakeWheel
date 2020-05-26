package com.example.wakewheel.facerecognition

import com.example.wakewheel.monitoring.EyesMeasurement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class EyesMeasurementEventBus {

    private val channel = BroadcastChannel<EyesMeasurement>(1)

    fun send(data: EyesMeasurement) {
        MainScope().launch {
            channel.send(data)
        }
    }

    fun listen(): BroadcastChannel<EyesMeasurement> =
        channel
}
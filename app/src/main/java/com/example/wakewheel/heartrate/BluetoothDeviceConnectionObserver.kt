package com.example.wakewheel.heartrate

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BluetoothDeviceConnectionObserver(
    private val api: BleDeviceConnectionApi
) {

    private var confirmConnectionJob: Job? = null

    fun confirmConnection() {
        cancelCountdown()
        api.setConnected(true)
        startCountdown()
    }

    private fun cancelCountdown() {
        confirmConnectionJob?.cancel()
        confirmConnectionJob = null
    }

    private fun startCountdown() {
        confirmConnectionJob = MainScope().launch {
            delay(CONNECTION_ABSENT_THRESHOLD)
            api.setConnected(false)
        }
    }

    companion object {
        private const val CONNECTION_ABSENT_THRESHOLD = 5_000L
    }
}
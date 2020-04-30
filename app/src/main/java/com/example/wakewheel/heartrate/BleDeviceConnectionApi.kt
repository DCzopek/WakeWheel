package com.example.wakewheel.heartrate

import androidx.lifecycle.LiveData

interface BleDeviceConnectionApi {
    fun setConnected(connected: Boolean)
    val deviceConnection: LiveData<Boolean>
}
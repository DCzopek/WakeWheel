package com.example.wakewheel.heartrate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class InMemoryBleDeviceConnectionRepo : BleDeviceConnectionApi {

    private var connected = MutableLiveData(false)
    private var autoConnected = false

    override val deviceConnection: LiveData<Boolean>
        get() = connected

    override fun setConnected(connected: Boolean) {
        this.connected.postValue(connected)
    }

    override fun setAutoConnected(connected: Boolean) {
        autoConnected = connected
    }

    override fun autoConnected(): Boolean =
        autoConnected
}
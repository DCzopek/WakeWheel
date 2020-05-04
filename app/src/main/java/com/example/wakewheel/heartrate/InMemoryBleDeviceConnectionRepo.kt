package com.example.wakewheel.heartrate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class InMemoryBleDeviceConnectionRepo : BleDeviceConnectionApi {

    private var connected = MutableLiveData(false)

    override val deviceConnection: LiveData<Boolean>
        get() = connected

    override fun setConnected(connected: Boolean) {
        this.connected.postValue(connected)
    }
}
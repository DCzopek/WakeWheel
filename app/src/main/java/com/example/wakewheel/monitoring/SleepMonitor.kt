package com.example.wakewheel.monitoring

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wakewheel.receivers.HeartRateEventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SleepMonitor(
    private val heartRateEventBus: HeartRateEventBus
) {

    val alarm: LiveData<Boolean>
        get() = _alarm

    private val _alarm = MutableLiveData(false)

    fun startMonitoring() {
        _alarm.postValue(true)
    }

    fun stopMonitoring() {
        _alarm.postValue(false)
    }
}
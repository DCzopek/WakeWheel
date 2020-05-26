package com.example.wakewheel.heartrate

import com.example.wakewheel.Const
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AutoConnectBleDevice(
    private val connectBleDevice: ConnectBleDevice,
    private val heartRateEventBus: HeartRateEventBus,
    private val deviceRepo: BluetoothDeviceRepo,
    private val connectionApi: BleDeviceConnectionApi
) {

    private var heartRateListen: Job? = null
    private var connected = false
    private val connectionObserver = { connected: Boolean -> this.connected = connected }

    init {
        connectionApi.deviceConnection
            .observeForever(connectionObserver)
    }

    operator fun invoke() {
        if (!connected) {
            deviceRepo.fetch()
                ?.let {
                    listenForHeartRate()
                    tryToConnect(it)
                }
        }
    }

    private fun listenForHeartRate() {
        heartRateListen = MainScope().launch {
            heartRateEventBus.listen()
                .openSubscription()
                .consumeEach {
                    connected = true
                    heartRateListen?.cancel()
                }
        }
    }

    private fun tryToConnect(device: BleDevice) {
        MainScope().launch {
            for (i in 0..5) {
                if (connected) {
                    stopObserveConnection()
                    break
                }
                connectBleDevice(device.address)
                delay(Const.BLUETOOTH_CONNECTION_TIMEOUT)
            }
            heartRateListen?.cancel()
            stopObserveConnection()
        }
    }

    private fun stopObserveConnection() {
        connectionApi.deviceConnection
            .removeObserver(connectionObserver)
    }
}

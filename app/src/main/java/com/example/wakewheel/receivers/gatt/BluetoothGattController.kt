package com.example.wakewheel.receivers.gatt

import com.example.wakewheel.heartrate.BluetoothDeviceConnectionObserver
import com.example.wakewheel.receivers.HeartRateEventBus
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.GATT_CONNECTED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.GATT_DISCONNECTED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.HEART_SERVICE_DISCOVERED
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class BluetoothGattController(
    private val gattEventBus: BluetoothGattEventBus,
    private val heartRateEventBus: HeartRateEventBus,
    private val connectionObserver: BluetoothDeviceConnectionObserver
) {

    fun onGattConnected() {
        gattEventBus.send(GATT_CONNECTED)
    }

    fun onGattDisconnected() {
        gattEventBus.send(GATT_DISCONNECTED)
    }

    fun onHeartServiceDiscovered() {
        gattEventBus.send(HEART_SERVICE_DISCOVERED)
    }

    fun onDataReceive(value: Int) {
        heartRateEventBus.send(value)
        connectionObserver.confirmConnection()
    }
}

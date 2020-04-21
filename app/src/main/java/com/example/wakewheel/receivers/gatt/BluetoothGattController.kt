package com.example.wakewheel.receivers.gatt

import com.example.wakewheel.receivers.gatt.BluetoothGattAction.DATA_RECEIVE
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.GATT_CONNECTED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.GATT_DISCONNECTED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.HEART_SERVICE_DISCOVERED
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class BluetoothGattController(
    private val eventBus: BluetoothGattEventBus
) {

    suspend fun onDataReceive() {
        eventBus.send(DATA_RECEIVE)
    }

    suspend fun onGattConnected() {
        eventBus.send(GATT_CONNECTED)
    }

    suspend fun onGattDisconnected() {
        eventBus.send(GATT_DISCONNECTED)
    }

    suspend fun onHeartServiceDiscovered() {
        eventBus.send(HEART_SERVICE_DISCOVERED)
    }
}

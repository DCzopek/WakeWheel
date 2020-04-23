package com.example.wakewheel.receivers.gatt

import com.example.wakewheel.receivers.HeartRateEventBus
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.GATT_CONNECTED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.GATT_DISCONNECTED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.HEART_SERVICE_DISCOVERED
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class BluetoothGattController(
    private val gattEventBus: BluetoothGattEventBus,
    private val heartRateEventBus: HeartRateEventBus
) {

    suspend fun onDataReceive(value: String) {
        heartRateEventBus.send(value)
    }

    suspend fun onGattConnected() {
        gattEventBus.send(GATT_CONNECTED)
    }

    suspend fun onGattDisconnected() {
        gattEventBus.send(GATT_DISCONNECTED)
    }

    suspend fun onHeartServiceDiscovered() {
        gattEventBus.send(HEART_SERVICE_DISCOVERED)
    }
}

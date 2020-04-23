package com.example.wakewheel.receivers.gatt

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel

@ExperimentalCoroutinesApi
class BluetoothGattEventBus {

    private val channel = BroadcastChannel<BluetoothGattAction>(1)

    suspend fun send(action: BluetoothGattAction) {
        channel.send(action)
    }

    fun listen(): BroadcastChannel<BluetoothGattAction> =
        channel

}

enum class BluetoothGattAction {
    GATT_CONNECTED,
    GATT_DISCONNECTED,
    HEART_SERVICE_DISCOVERED
}
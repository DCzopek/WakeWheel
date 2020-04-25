package com.example.wakewheel.receivers.gatt

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class BluetoothGattEventBus {

    private val channel = BroadcastChannel<BluetoothGattAction>(1)

    fun send(action: BluetoothGattAction) {
        MainScope().launch {
            channel.send(action)
        }
    }

    fun listen(): BroadcastChannel<BluetoothGattAction> =
        channel

}

enum class BluetoothGattAction {
    GATT_CONNECTED,
    GATT_DISCONNECTED,
    HEART_SERVICE_DISCOVERED,
    SET_NOTIFICATION_FAILS,
    SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE,
    CONNECT_TO_HEART_RATE_DEVICE_SUCCEED
}
package com.example.wakewheel.heartrate

import com.example.wakewheel.Const
import com.example.wakewheel.receivers.gatt.BluetoothGattAction
import com.example.wakewheel.receivers.gatt.BluetoothGattEventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ConnectBleDevice(
    private val bleHandler: BleHandler,
    private val gattEventBus: BluetoothGattEventBus
) {

    private var listenForHeartRateService: Job? = null

    operator fun invoke(address: String) {
        MainScope().launch {
            bleHandler.connectDevice(address)
            lazyConnectHeartRateService()

            delay(Const.BLUETOOTH_CONNECTION_TIMEOUT)

            listenForHeartRateService?.let {
                if (it.isActive) {
                    it.cancel()
                }
            }
        }
    }

    private fun lazyConnectHeartRateService() {
        listenForHeartRateService = MainScope().launch {
            gattEventBus.listen()
                .openSubscription()
                .consumeEach {
                    if (it == BluetoothGattAction.HEART_SERVICE_DISCOVERED) {
                        bleHandler.connectHeartRateService()
                        listenForHeartRateService?.cancel()
                    }
                }
        }
    }
}

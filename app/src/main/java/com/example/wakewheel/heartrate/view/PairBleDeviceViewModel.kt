package com.example.wakewheel.heartrate.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wakewheel.heartrate.BleDevice
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.DURING
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.FAIL
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.NONE
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.SUCCESS
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.TIMEOUT
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.CONNECT_TO_HEART_RATE_DEVICE_SUCCEED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.HEART_SERVICE_DISCOVERED
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.SET_NOTIFICATION_FAILS
import com.example.wakewheel.receivers.gatt.BluetoothGattAction.SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE
import com.example.wakewheel.receivers.gatt.BluetoothGattEventBus
import com.example.wakewheel.utils.SingleLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PairBleDeviceViewModel @Inject constructor(
    private val bleHandler: BleHandler,
    private val gattEventBus: BluetoothGattEventBus
) : ViewModel() {

    private var listenForHeartRateService: Job? = null
    private var listenForGattEvents: Job? = null

    val deviceConnection: LiveData<DeviceConnectionStatus>
        get() = _deviceConnection

    private val _deviceConnection = MutableLiveData(NONE)

    val deviceList: LiveData<List<BleDevice>>
        get() = _deviceList

    private val _deviceList = MutableLiveData<List<BleDevice>>()

    val requestBluetoothEnable: LiveData<Any>
        get() = _requestBluetoothEnable

    private val _requestBluetoothEnable = SingleLiveEvent<Any>()

    val showToast: LiveData<String>
        get() = _showToast

    private val _showToast = SingleLiveEvent<String>()

    init {
        listenForGattEvents = MainScope().launch {
            gattEventBus.listen()
                .openSubscription()
                .consumeEach {
                    when (it) {
                        SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE,
                        SET_NOTIFICATION_FAILS -> _deviceConnection.postValue(FAIL)
                        CONNECT_TO_HEART_RATE_DEVICE_SUCCEED -> _deviceConnection.postValue(SUCCESS)
                        else -> {
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenForGattEvents?.cancel()
        listenForGattEvents = null
    }

    fun onScanClick() {
        if (bleHandler.isBluetoothEnabled()) {
            MainScope().launch {
                bleHandler.scanForBle()
                    .let { devices -> _deviceList.postValue(devices) }
            }
        } else {
            _requestBluetoothEnable.call()
        }
    }

    fun onPairClicked(macAddress: String) {
        _deviceConnection.postValue(DURING)
        MainScope().launch {
            bleHandler.connectDevice(macAddress)
            lazyConnectHeartRateService()

            delay(CONNECTION_TIMEOUT)

            listenForHeartRateService?.let {
                if (it.isActive) {
                    _deviceConnection.postValue(TIMEOUT)
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
                    if (it == HEART_SERVICE_DISCOVERED) {
                        bleHandler.connectHeartRateService()
                        listenForHeartRateService?.cancel()
                    }
                }
        }
    }

    companion object {
        private const val CONNECTION_TIMEOUT = 30_000L
    }
}

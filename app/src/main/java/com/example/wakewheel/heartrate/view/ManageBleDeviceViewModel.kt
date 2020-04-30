package com.example.wakewheel.heartrate.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wakewheel.heartrate.BleDevice
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.BluetoothDeviceRepo
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.DURING
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.FAIL
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.SUCCESS
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.TIMEOUT
import com.example.wakewheel.receivers.HeartRateEventBus
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
class ManageBleDeviceViewModel @Inject constructor(
    private val bleHandler: BleHandler,
    private val gattEventBus: BluetoothGattEventBus,
    private val heartRateEventBus: HeartRateEventBus,
    private val repo: BluetoothDeviceRepo
) : ViewModel() {

    private var listenForHeartRateService: Job? = null
    private var listenForGattEvents: Job? = null
    private var scanJob: Job? = null

    val deviceConnection: SingleLiveEvent<DeviceConnectionStatus>
        get() = _deviceConnection

    private val _deviceConnection = SingleLiveEvent<DeviceConnectionStatus>()

    val deviceList: LiveData<List<BleDevice>>
        get() = _deviceList

    private val _deviceList = MutableLiveData<List<BleDevice>>()

    val requestBluetoothEnable: LiveData<Any>
        get() = _requestBluetoothEnable

    private val _requestBluetoothEnable = SingleLiveEvent<Any>()

    init {
        listenForGattEvents = MainScope().launch {
            gattEventBus.listen()
                .openSubscription()
                .consumeEach {
                    when (it) {
                        SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE,
                        SET_NOTIFICATION_FAILS -> _deviceConnection.postValue(FAIL)
                        CONNECT_TO_HEART_RATE_DEVICE_SUCCEED -> handleSuccess()
                        else -> {
                        }
                    }
                }
        }
    }

    private fun handleSuccess() {
        var dataReceived = false
        val channel = heartRateEventBus.listen()
            .openSubscription()

        MainScope().launch {
            channel
                .receive()
                .let {
                    dataReceived = true
                    _deviceConnection.postValue(SUCCESS)
                    saveDevice()
                    channel.cancel()
                }
        }

        MainScope().launch {
            delay(5000L)
            if (!dataReceived) {
                _deviceConnection.postValue(DeviceConnectionStatus.NO_HEART_RATE)
            }
            channel.cancel()
        }
    }

    private fun saveDevice() {
        bleHandler.getCurrentlyConnectedDevice()
            ?.let { repo.insertOrUpdate(it) }
    }

    override fun onCleared() {
        super.onCleared()
        listenForGattEvents?.cancel()
        listenForGattEvents = null
        stopScanning()
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

    fun startScanning() {
        scanJob = MainScope().launch {
            while (true) {
                performScan()
                delay(SCAN_PERIOD_TIME)
            }
        }
    }

    fun stopScanning() {
        scanJob?.cancel()
    }

    private fun performScan() {
        if (bleHandler.isBluetoothEnabled()) {
            MainScope().launch {
                bleHandler.scanForBle()
                    .let { devices -> _deviceList.postValue(devices) }
            }
        } else {
            _requestBluetoothEnable.call()
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
        private const val SCAN_PERIOD_TIME = 15_000L
    }
}

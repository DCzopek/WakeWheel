package com.example.wakewheel.heartrate.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wakewheel.Const
import com.example.wakewheel.heartrate.BleDevice
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.BluetoothDeviceRepo
import com.example.wakewheel.heartrate.ConnectBleDevice
import com.example.wakewheel.heartrate.HeartRateEventBus
import com.example.wakewheel.heartrate.receivers.BluetoothGattAction.CONNECT_TO_HEART_RATE_DEVICE_SUCCEED
import com.example.wakewheel.heartrate.receivers.BluetoothGattAction.SET_NOTIFICATION_FAILS
import com.example.wakewheel.heartrate.receivers.BluetoothGattAction.SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE
import com.example.wakewheel.heartrate.receivers.BluetoothGattEventBus
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.DURING
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.FAIL
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.SUCCESS
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.TIMEOUT
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
    private val repo: BluetoothDeviceRepo,
    private val connectBleDevice: ConnectBleDevice
) : ViewModel() {

    private var listenForGattEvents: Job? = null
    private var scanJob: Job? = null

    private var pairing = false

    val deviceConnection = SingleLiveEvent<DeviceConnectionStatus>()

    val deviceList: LiveData<List<BleDevice>>
        get() = _deviceList

    private val _deviceList = MutableLiveData<List<BleDevice>>()

    val requestBluetoothEnable = SingleLiveEvent<Any>()

    init {
        listenForGattEvents = MainScope().launch {
            gattEventBus.listen()
                .openSubscription()
                .consumeEach {
                    when (it) {
                        SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE,
                        SET_NOTIFICATION_FAILS -> deviceConnection.postValue(FAIL)
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
                    deviceConnection.postValue(SUCCESS)
                    channel.cancel()
                    if (pairing) {
                        saveDevice()
                        pairing = false
                    }
                }
        }

        MainScope().launch {
            delay(5000L)
            if (!dataReceived) {
                deviceConnection.postValue(DeviceConnectionStatus.NO_HEART_RATE)
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

    fun onConnectClicked() {
        repo.fetch()
            ?.let { connect(it.address) }
    }

    fun onPairClicked(macAddress: String) {
        pairing = true
        connect(macAddress)
    }

    private fun connect(macAddress: String) {
        deviceConnection.postValue(DURING)
        MainScope().launch {
            connectBleDevice(macAddress)
            delay(Const.BLUETOOTH_CONNECTION_TIMEOUT)
            if (deviceConnection.value == DURING) {
                deviceConnection.postValue(TIMEOUT)
            }
        }
    }

    fun startScanning() {
        scanJob = MainScope().launch {
            if (bleHandler.isBluetoothEnabled()) {
                while (true) {
                    performScan()
                    delay(SCAN_PERIOD_TIME)
                }

            } else {
                requestBluetoothEnable.call()
            }
        }
    }

    private fun performScan() {
        MainScope().launch {
            bleHandler.scanForBle()
                .let { devices -> _deviceList.postValue(devices) }
        }
    }

    fun stopScanning() {
        scanJob?.cancel()
    }

    fun getPairedDevice(): BleDevice? =
        repo.fetch()

    companion object {
        private const val SCAN_PERIOD_TIME = 15_000L
    }
}

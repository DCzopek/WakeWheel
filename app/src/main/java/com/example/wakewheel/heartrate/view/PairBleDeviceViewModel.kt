package com.example.wakewheel.heartrate.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wakewheel.heartrate.BleDevice
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.utils.SingleLiveEvent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PairBleDeviceViewModel @Inject constructor(
    private val bleHandler: BleHandler
) : ViewModel() {

    val deviceList: LiveData<List<BleDevice>>
        get() = _deviceList

    private val _deviceList = MutableLiveData<List<BleDevice>>()

    val requestBluetoothEnable: LiveData<Any>
        get() = _startActivity

    private val _startActivity = SingleLiveEvent<Any>()

    fun onScanClick() {
        if (bleHandler.isBluetoothEnabled()) {
            MainScope().launch {
                bleHandler.scanForBle()
                    .let { devices -> _deviceList.postValue(devices) }
            }
        } else {
            _startActivity.call()
        }

    }

    fun onPairClicked(macAddress: String) {
        MainScope().launch {
            bleHandler.connectDevice(macAddress)
                .let { success ->
                    if (success) println("Successfully verify device - can pair")
                    else println("Fail verifying device - not paired")
                }
        }
    }
}

package com.example.wakewheel.heartrate

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.example.wakewheel.services.BluetoothLeService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import java.util.HashSet

@ExperimentalCoroutinesApi
class BleHandler(
    val context: Context,
    private val service: BluetoothLeService
) {

    // todo ograc to gdyby jakims cudem urzadzenie nie wspieralo bluetootha
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val deviceList: HashSet<BluetoothDevice> = hashSetOf()

    fun connectDevice(deviceMac: String) {
        deviceList
            .first { it.address == deviceMac }
            .let { service.connectDevice(it) }
    }

    fun connectHeartRateService() {
        service.connectToHeartRateService()
    }

    suspend fun scanForBle(): List<BleDevice> {
        bluetoothLeScanner.startScan(leScanCallback)
        delay(SCAN_PERIOD)
        bluetoothLeScanner.stopScan(leScanCallback)
        return deviceList.toList().map { BleDevice(it.name ?: "Undefined", it.address) }
    }

    private val leScanCallback = object : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            println("device ${result?.device?.name ?: "Undefined"} with MAC : ${result?.device?.address}")
            result?.device?.let { deviceList.add(it) }
        }

    }

    fun isBluetoothEnabled(): Boolean =
        bluetoothAdapter.isEnabled

    companion object {
        private const val SCAN_PERIOD: Long = 5000
    }
}

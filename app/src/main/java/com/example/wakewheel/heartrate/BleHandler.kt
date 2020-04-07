package com.example.wakewheel.heartrate

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.example.wakewheel.services.BluetoothLeService
import kotlinx.coroutines.delay
import java.util.HashSet
import java.util.UUID

class BleHandler(
    val context: Context,
    private val service: BluetoothLeService
) {

    // todo ograc to gdyby jakims cudem urzadzenie nie wspieralo bluetootha
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private lateinit var bluetoothGatt: BluetoothGatt
    private val deviceList: HashSet<BluetoothDevice> = hashSetOf()

    fun tryConnect(deviceMac: String) {
        connectGatt(deviceMac)
    }

    // todo Check if the bluetooth is on, if not request to turn on
    suspend fun scanForBle(): List<BleDevice> {
        bluetoothLeScanner.startScan(leScanCallback)
        delay(SCAN_PERIOD)
        bluetoothLeScanner.stopScan(leScanCallback)
        return deviceList.toList().map { BleDevice(it.name, it.address) }
    }

    fun setNotification(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt.setCharacteristicNotification(characteristic, true)
        val uuid: UUID = UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG)
        val descriptor = characteristic.getDescriptor(uuid)
            .apply {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }
        bluetoothGatt.writeDescriptor(descriptor)
    }

    private val leScanCallback = object : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            println("device ${result?.device?.name} with MAC : ${result?.device?.address}")
            result?.device?.let { deviceList.add(it) }
        }

    }

    private fun connectGatt(deviceMac: String) {
        bluetoothGatt = deviceList.first { it.address == deviceMac }
            .connectGatt(context, true, service.gattCallback)
        service.bluetoothGatt = bluetoothGatt
    }

    fun isBluetoothEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    companion object {
        private const val SCAN_PERIOD: Long = 5000
    }
}

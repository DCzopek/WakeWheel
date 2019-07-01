package com.example.wakewheel

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import io.reactivex.Completable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class HeartRate(
    val context: Context,
    private val service: BluetoothLeService
) {
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private lateinit var bluetoothGatt: BluetoothGatt

    private val leScanCallback = object : ScanCallback() {
        val deviceList: HashSet<BluetoothDevice> = hashSetOf()

        @SuppressLint("CheckResult")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Completable
                .fromAction {
                    println("device ${result?.device?.name} with MAC : ${result?.device?.address}")
                    result?.device?.let { deviceList.add(it) }
                }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    Functions.EMPTY_ACTION,
                    Functions.emptyConsumer()
                )
        }

    }

    @SuppressLint("CheckResult")
    fun scanForBle() {

        Completable.fromAction {
            println("Scan started")
            bluetoothLeScanner.startScan(leScanCallback)
        }
            .delay(SCAN_PERIOD, TimeUnit.SECONDS)
            .andThen(Completable.fromAction {
                println("Scan Stopped")
                bluetoothLeScanner.stopScan(leScanCallback)
            })
            .subscribe(
                Functions.EMPTY_ACTION,
                Functions.emptyConsumer()
            )
    }


    fun connectGatt() {
        bluetoothGatt = leScanCallback.deviceList.first().connectGatt(context, true, service.gattCallback)
        service.bluetoothGatt = bluetoothGatt
    }

    fun setNotification(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt.setCharacteristicNotification(characteristic, true)
        val uuid: UUID = UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG)
        val descriptor = characteristic.getDescriptor(uuid).apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        bluetoothGatt.writeDescriptor(descriptor)
    }

    companion object {
        private const val SCAN_PERIOD: Long = 10
    }

}

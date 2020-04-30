package com.example.wakewheel.heartrate

import android.bluetooth.BluetoothDevice

interface BluetoothDeviceRepo {
    fun insertOrUpdate(device: BluetoothDevice)
    fun insertOrUpdate(device: BleDevice)
    fun remove(device: BluetoothDevice)
    fun remove(device: BleDevice)
    fun fetch(): BleDevice?
}
package com.example.wakewheel.services

import android.os.Binder

class BluetoothLeServiceBinder : Binder() {
    fun getService(bluetoothLeService: BluetoothLeService): BluetoothLeService = bluetoothLeService
}
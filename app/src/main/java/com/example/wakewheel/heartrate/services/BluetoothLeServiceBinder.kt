package com.example.wakewheel.heartrate.services

import android.os.Binder
import com.example.wakewheel.heartrate.services.BluetoothLeService

class BluetoothLeServiceBinder : Binder() {
    fun getService(bluetoothLeService: BluetoothLeService): BluetoothLeService = bluetoothLeService
}
package com.example.wakewheel.services

import com.example.wakewheel.hr.BluetoothLeService
import android.os.Binder


class BluetoothLeServiceBinder : Binder() {
    fun getService(bluetoothLeService: BluetoothLeService): BluetoothLeService = bluetoothLeService
}
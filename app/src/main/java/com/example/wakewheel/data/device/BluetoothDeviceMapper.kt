package com.example.wakewheel.data.device

import android.bluetooth.BluetoothDevice
import com.example.wakewheel.heartrate.BleDevice

class BluetoothDeviceMapper {

    fun map(data: BluetoothDevice): RealmBluetoothDevice =
        RealmBluetoothDevice(
            address = data.address,
            name = data.name
        )

    fun mapToDomain(data: BluetoothDevice): BleDevice =
        BleDevice(
            address = data.address,
            name = data.name
        )

    fun map(data: BleDevice): RealmBluetoothDevice =
        RealmBluetoothDevice(
            name = data.name,
            address = data.address
        )

    fun map(data: RealmBluetoothDevice): BleDevice =
        BleDevice(
            name = data.name,
            address = data.address!!
        )
}
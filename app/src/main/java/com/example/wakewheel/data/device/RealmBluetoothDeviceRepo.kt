package com.example.wakewheel.data.device

import android.bluetooth.BluetoothDevice
import com.example.wakewheel.heartrate.BleDevice
import com.example.wakewheel.heartrate.BluetoothDeviceRepo
import io.realm.Realm

class RealmBluetoothDeviceRepo(
    private val realm: Realm,
    private val mapper: BluetoothDeviceMapper
) : BluetoothDeviceRepo {

    override fun insertOrUpdate(device: BluetoothDevice) {
        insertOrUpdate(mapper.mapToDomain(device))
    }

    override fun insertOrUpdate(device: BleDevice) {
        realm.executeTransaction {
            it.insertOrUpdate(mapper.map(device))
        }
    }

    override fun remove(device: BluetoothDevice) {
        remove(mapper.mapToDomain(device))
    }

    override fun remove(device: BleDevice) {
        realm.executeTransaction {
            it.where(RealmBluetoothDevice::class.java)
                .equalTo("address", device.address)
                .findFirst()
                ?.deleteFromRealm()
        }
    }

    override fun fetch(): BleDevice? =
        realm.where(RealmBluetoothDevice::class.java)
            .findFirst()
            ?.let { mapper.map(realm.copyFromRealm(it)) }
}
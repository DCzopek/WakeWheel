package com.example.wakewheel.data.device

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmBluetoothDevice(
    @PrimaryKey var address: String? = null,
    var name: String = ""
) : RealmObject()
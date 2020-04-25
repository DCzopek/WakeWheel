package com.example.wakewheel.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Calendar
import java.util.Date
import java.util.UUID

// todo before delete this, try inject realm instance
open class RealmHeartRateData(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var heartRate: Int = 0,
    var date: Date = Calendar.getInstance().time
) : RealmObject()
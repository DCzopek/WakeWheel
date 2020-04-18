package com.example.wakewheel.dbtest

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Calendar
import java.util.Date
import java.util.UUID

open class RealmHeartRateData(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var heartRate: Long = 0,
    var date: Date = Calendar.getInstance().time
) : RealmObject()
package com.example.wakewheel.data

import com.example.wakewheel.monitoring.AlarmReason
import com.example.wakewheel.monitoring.AlarmReasonRepo

class InMemoryAlarmReasonRepo : AlarmReasonRepo {

    private var lastReason: AlarmReason? = null

    override fun insertLastAlarmReason(reason: AlarmReason) {
        lastReason = reason
    }

    override fun fetchLastAlarmReason(): AlarmReason? =
        lastReason
}
package com.example.wakewheel.monitoring

interface AlarmReasonRepo {
    fun insertLastAlarmReason(reason: AlarmReason)
    fun fetchLastAlarmReason(): AlarmReason?
}
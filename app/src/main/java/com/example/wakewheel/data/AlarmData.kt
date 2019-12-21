package com.example.wakewheel.data

import java.util.Date

data class AlarmData(
    val date: Date,
    val alarmReason: AlarmReason,
    val isProper: Boolean
)
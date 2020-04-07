package com.example.wakewheel.alarm

import java.util.Date

data class AlarmData(
    val date: Date,
    val alarmReason: AlarmReason,
    val isProper: Boolean
)
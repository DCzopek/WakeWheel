package com.example.wakewheel.monitoring

data class HeartRateSpecification(
    val timeThreshold: Long = 5_000L,
    val valueThreshold: Int = 55
)
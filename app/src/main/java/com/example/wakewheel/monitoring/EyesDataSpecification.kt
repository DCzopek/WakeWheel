package com.example.wakewheel.monitoring

data class EyesDataSpecification(
    val timeThreshold: Long = 2_000L,
    val valueThreshold: Float = 0.3f
)
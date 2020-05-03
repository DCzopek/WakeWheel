package com.example.wakewheel.monitoring

interface SpecificationsRepo {
    fun insertOrUpdate(data: HeartRateSpecification)
    fun insertOrUpdate(data: EyesDataSpecification)
    fun fetchHeartRateSpecification(): HeartRateSpecification?
    fun fetchEyesDataSpecification(): EyesDataSpecification?
}
package com.example.wakewheel.monitoring

class AlarmSpecificationChecker {

    private var heartRateCounting = false
    private var eyesDataCounting = false

    private var heartRateCountdownTimestamp = 0L
    private var eyesDataCountdownTimestamp = 0L

    private var eyesDataSpecification = EyesDataSpecification()
    private var heartRateSpecification = HeartRateSpecification()

    fun refreshSpecificationData() {
    }

    fun checkHeartRateData(data: Double): Boolean =
        if (isBelowThreshold(data)) {
            heartRateSpecification.checkHeartRateTimeSpecification()
        } else {
            heartRateCounting = false
            false
        }

    fun checkEyesData(data: EyesMeasurement): Boolean =
        if (isBelowThreshold(data)) {
            eyesDataSpecification.checkEyesDataTimeSpecification()
        } else {
            eyesDataCounting = false
            false
        }

    fun isBelowThreshold(data: EyesMeasurement): Boolean =
        with(eyesDataSpecification) {
            data.leftOpenProbability < valueThreshold && data.rightOpenProbability < valueThreshold
        }

    private fun EyesDataSpecification.checkEyesDataTimeSpecification(): Boolean =
        System.currentTimeMillis()
            .let { now ->
                if (eyesDataCounting) {
                    timeThreshold < now - eyesDataCountdownTimestamp
                } else {
                    eyesDataCounting = true
                    eyesDataCountdownTimestamp = now
                    false
                }
            }

    fun isBelowThreshold(data: Double): Boolean =
        data < heartRateSpecification.valueThreshold

    private fun HeartRateSpecification.checkHeartRateTimeSpecification(): Boolean =
        System.currentTimeMillis()
            .let { now ->
                if (heartRateCounting) {
                    timeThreshold < now - heartRateCountdownTimestamp
                } else {
                    heartRateCounting = true
                    heartRateCountdownTimestamp = now
                    false
                }
            }
}

package com.example.wakewheel.data.specifications

import com.example.wakewheel.monitoring.EyesDataSpecification
import com.example.wakewheel.monitoring.HeartRateSpecification

class SpecificationsMapper {

    fun map(data: HeartRateSpecification): RealmHeartRateSpecification =
        with(data) {
            RealmHeartRateSpecification(
                timeThreshold = timeThreshold,
                valueThreshold = valueThreshold
            )
        }

    fun map(data: RealmHeartRateSpecification): HeartRateSpecification =
        with(data) {
            HeartRateSpecification(
                timeThreshold = timeThreshold!!,
                valueThreshold = valueThreshold!!
            )
        }

    fun map(data: EyesDataSpecification): RealmEyesDataSpecification =
        with(data) {
            RealmEyesDataSpecification(
                timeThreshold = timeThreshold,
                valueThreshold = valueThreshold
            )
        }

    fun map(data: RealmEyesDataSpecification): EyesDataSpecification =
        with(data) {
            EyesDataSpecification(
                timeThreshold = timeThreshold!!,
                valueThreshold = valueThreshold!!
            )
        }
}

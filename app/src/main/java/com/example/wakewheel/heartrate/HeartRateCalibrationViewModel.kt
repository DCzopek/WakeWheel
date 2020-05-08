package com.example.wakewheel.heartrate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wakewheel.monitoring.HeartRateSpecification
import com.example.wakewheel.monitoring.SpecificationsRepo
import javax.inject.Inject

class HeartRateCalibrationViewModel @Inject constructor(
    private val specificationsRepo: SpecificationsRepo
) : ViewModel() {

    private val defaultHeartRateSpecification = HeartRateSpecification()

    val currentHeartRateValueThreshold: LiveData<Int?>
        get() = _currentHeartRateValueThreshold

    private val _currentHeartRateValueThreshold = MutableLiveData<Int?>()

    init {
        specificationsRepo.fetchHeartRateSpecification()
            ?.let { _currentHeartRateValueThreshold.postValue(it.valueThreshold) }
    }

    fun setCurrentHeartRateValueThreshold(value: Int) {
        _currentHeartRateValueThreshold.postValue(value)
        specificationsRepo.insertOrUpdate(
            HeartRateSpecification(
                valueThreshold = value,
                timeThreshold = getCurrentHeartRateTimeThreshold()
            )
        )
    }

    private fun getCurrentHeartRateTimeThreshold(): Long =
        specificationsRepo.fetchHeartRateSpecification()?.timeThreshold
            ?: defaultHeartRateSpecification.timeThreshold
}
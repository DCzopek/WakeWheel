package com.example.wakewheel.monitoring

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wakewheel.monitoring.AlarmReason.EYES_CLOSURE
import com.example.wakewheel.monitoring.AlarmReason.HEART_RATE
import com.example.wakewheel.monitoring.MonitorParameterStatus.DANGER
import com.example.wakewheel.monitoring.MonitorParameterStatus.OK
import com.example.wakewheel.receivers.EyesMeasurementEventBus
import com.example.wakewheel.receivers.HeartRateEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class SleepMonitor(
    private val heartRateEventBus: HeartRateEventBus,
    private val eyesMeasurementEventBus: EyesMeasurementEventBus,
    private val alarmSpecificationChecker: AlarmSpecificationChecker
) {

    val monitoring: LiveData<Boolean>
        get() = _monitoring

    private val _monitoring = MutableLiveData(false)

    private var heartRateListen: Job? = null
    private var eyesMeasurementListen: Job? = null

    private val leftEyeMeasurements = mutableMapOf<Long, Float>()
    private val rightEyeMeasurements = mutableMapOf<Long, Float>()
    private val heartRateMeasurements = mutableMapOf<Long, Int>()

    val alarm: LiveData<AlarmReason>
        get() = _alarm

    private val _alarm = MutableLiveData<AlarmReason>()

    val heartRateStatus: LiveData<MonitorParameterStatus>
        get() = _heartRateStatus

    private val _heartRateStatus = MutableLiveData(OK)

    val eyesClosureStatus: LiveData<MonitorParameterStatus>
        get() = _eyesClosureStatus

    private val _eyesClosureStatus = MutableLiveData(OK)

    fun startMonitoring() {
        _monitoring.value = true
        startHeartRateListen()
        startEyesMeasurementListen()
        startCheckingSpecifications()
        alarmSpecificationChecker.refreshSpecificationData()
    }

    fun stopMonitoring() {
        heartRateMeasurements.clear()
        leftEyeMeasurements.clear()
        rightEyeMeasurements.clear()
        _monitoring.value = false
        _eyesClosureStatus.postValue(OK)
        _heartRateStatus.postValue(OK)
        heartRateListen?.cancel()
        eyesMeasurementListen?.cancel()
    }

    private fun startCheckingSpecifications() {
        CoroutineScope(Dispatchers.IO).launch {
            while (_monitoring.value!!) {
                delay(250L)
                when {
                    heartRateIsSatisfied() -> _alarm.postValue(HEART_RATE)
                    eyesDataIsSatisfied() -> _alarm.postValue(EYES_CLOSURE)
                }
                delay(250L)
                _heartRateStatus.postValue(getHeartRateStatus())
                _eyesClosureStatus.postValue(getEyesDataStatus())
            }
        }
    }

    private fun getHeartRateStatus(): MonitorParameterStatus =
        when {
            heartRateMeasurements.isEmpty() -> OK
            alarmSpecificationChecker.isBelowThreshold(currentHeartRate()) -> DANGER
            else -> OK
        }

    private fun getEyesDataStatus(): MonitorParameterStatus =
        when {
            leftEyeMeasurements.isEmpty() || rightEyeMeasurements.isEmpty() -> OK
            alarmSpecificationChecker.isBelowThreshold(currentEyesMeasurement()) -> DANGER
            else -> OK
        }

    private fun eyesDataIsSatisfied(): Boolean =
        alarmSpecificationChecker.checkEyesData(currentEyesMeasurement())

    private fun heartRateIsSatisfied() =
        alarmSpecificationChecker.checkHeartRateData(currentHeartRate())

    private fun currentHeartRate() =
        heartRateMeasurements.values.average()

    private fun currentEyesMeasurement(): EyesMeasurement =
        EyesMeasurement(
            leftOpenProbability = leftEyeMeasurements.values.average().toFloat(),
            rightOpenProbability = rightEyeMeasurements.values.average().toFloat()
        )

    private fun startHeartRateListen() {
        heartRateListen = CoroutineScope(Dispatchers.IO).launch {
            heartRateEventBus.listen()
                .openSubscription()
                .consumeEach {
                    println("SleepMonitor: heartRateMeasurement $it")
                    heartRateMeasurements.bufferedPut(timestamp(), it, DEFAULT_BUFFER_SIZE)
                }
        }
    }

    private fun startEyesMeasurementListen() {
        eyesMeasurementListen = CoroutineScope(Dispatchers.IO).launch {
            eyesMeasurementEventBus.listen()
                .openSubscription()
                .consumeEach {
                    leftEyeMeasurements
                        .bufferedPut(timestamp(), it.leftOpenProbability, DEFAULT_BUFFER_SIZE)
                    rightEyeMeasurements
                        .bufferedPut(timestamp(), it.rightOpenProbability, DEFAULT_BUFFER_SIZE)
                }
        }
    }

    private fun timestamp() =
        System.currentTimeMillis()

    /**
     * Add item to map. If buffer size is reached removes the oldest item then add new one.
     * @param timestampKey should be date in millis.
     */
    private fun <V> MutableMap<Long, V>.bufferedPut(timestampKey: Long, value: V, bufferSize: Int) {
        if (size < bufferSize) {
            this[timestampKey] = value
        } else {
            this.keys.min()
                ?.let { this.remove(it) }
            this[timestampKey] = value
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 5
    }
}
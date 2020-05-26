package com.example.wakewheel.monitoring

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wakewheel.monitoring.AlarmReason.EYES_CLOSURE
import com.example.wakewheel.monitoring.AlarmReason.HEART_RATE
import com.example.wakewheel.monitoring.MonitorParameterStatus.DANGER
import com.example.wakewheel.monitoring.MonitorParameterStatus.OK
import com.example.wakewheel.monitoring.MonitorParameterStatus.WARNING_NO_DATA_RECEIVED
import com.example.wakewheel.facerecognition.EyesMeasurementEventBus
import com.example.wakewheel.heartrate.HeartRateEventBus
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

    private var lastEyesDataReceiveTimestamp = 0L

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
                val hrData = currentHeartRate()
                val eyesData = currentEyesMeasurement()
                delay(250L)
                when {
                    heartRateIsSatisfied(hrData) -> _alarm.postValue(HEART_RATE)
                    eyesDataIsSatisfied(eyesData) -> _alarm.postValue(EYES_CLOSURE)
                }
                delay(250L)
                _heartRateStatus.postValue(getHeartRateStatus(hrData))
                _eyesClosureStatus.postValue(getEyesDataStatus(eyesData))
            }
        }
    }

    private fun getHeartRateStatus(data: Double): MonitorParameterStatus =
        when {
            heartRateMeasurements.isEmpty() -> OK
            alarmSpecificationChecker.isBelowThreshold(data) -> DANGER
            else -> OK
        }

    private fun getEyesDataStatus(data: EyesMeasurement): MonitorParameterStatus =
        when {
            leftEyeMeasurements.isEmpty() || rightEyeMeasurements.isEmpty() -> OK
            alarmSpecificationChecker.isBelowThreshold(data) -> DANGER
            isDataReceiveGap() -> WARNING_NO_DATA_RECEIVED
            else -> OK
        }

    private fun eyesDataIsSatisfied(data: EyesMeasurement): Boolean =
        alarmSpecificationChecker.checkEyesData(data)

    private fun heartRateIsSatisfied(data: Double) =
        alarmSpecificationChecker.checkHeartRateData(data)

    private fun currentHeartRate() =
        heartRateMeasurements.values.average()

    private fun isDataReceiveGap(): Boolean =
        lastEyesDataReceiveTimestamp != 0L && timestamp() - lastEyesDataReceiveTimestamp > EYES_DATA_RECEIVE_THRESHOLD

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
                    timestamp().let { timestamp ->
                        lastEyesDataReceiveTimestamp = timestamp
                        leftEyeMeasurements
                            .bufferedPut(timestamp, it.leftOpenProbability, DEFAULT_BUFFER_SIZE)
                        rightEyeMeasurements
                            .bufferedPut(timestamp, it.rightOpenProbability, DEFAULT_BUFFER_SIZE)
                    }
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
        private const val EYES_DATA_RECEIVE_THRESHOLD = 5_000L
    }
}
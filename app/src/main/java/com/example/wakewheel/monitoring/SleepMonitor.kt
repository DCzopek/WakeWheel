package com.example.wakewheel.monitoring

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private var monitoring = false

    private var heartRateListen: Job? = null
    private var eyesMeasurementListen: Job? = null

    private val leftEyeMeasurements = mutableMapOf<Long, Float>()
    private val rightEyeMeasurements = mutableMapOf<Long, Float>()
    private val heartRateMeasurements = mutableMapOf<Long, Int>()

    val alarm: LiveData<Boolean>
        get() = _alarm

    private val _alarm = MutableLiveData(false)

    fun startMonitoring() {
        monitoring = true
        startHeartRateListen()
        startEyesMeasurementListen()
        startCheckingSpecifications()
        alarmSpecificationChecker.refreshSpecificationData()
    }

    fun stopMonitoring() {
        monitoring = false
        _alarm.postValue(false)
        heartRateListen?.cancel()
        eyesMeasurementListen?.cancel()
    }

    private fun startCheckingSpecifications() {
        CoroutineScope(Dispatchers.IO).launch {
            while (monitoring) {
                when {
                    heartRateIsSatisfied() -> println("Start alarm caused by heartRate")
                    eyesDataIsSatisfied() -> println("Start alarm caused by eyes measurement")
                }
                delay(500L)
            }
        }
    }

    private fun eyesDataIsSatisfied(): Boolean =
        alarmSpecificationChecker.checkEyesData(
            EyesMeasurement(
                leftOpenProbability = leftEyeMeasurements.values.average().toFloat(),
                rightOpenProbability = rightEyeMeasurements.values.average().toFloat()
            )
        )

    private fun heartRateIsSatisfied() =
        alarmSpecificationChecker.checkHeartRateData(heartRateMeasurements.values.average())

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
package com.example.wakewheel.monitoring

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wakewheel.Const
import com.example.wakewheel.extensions.visionImageRotation
import com.example.wakewheel.heartrate.AutoConnectBleDevice
import com.example.wakewheel.monitoring.AlarmReason.EYES_CLOSURE
import com.example.wakewheel.monitoring.AlarmReason.HEART_RATE
import com.example.wakewheel.receivers.EyesMeasurementEventBus
import com.example.wakewheel.receivers.HeartRateEventBus
import com.example.wakewheel.utils.SingleLiveEvent
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.Frame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MonitoringViewModel @Inject constructor(
    private val sleepMonitor: SleepMonitor,
    private val eyesMeasurementEventBus: EyesMeasurementEventBus,
    private val heartRateEventBus: HeartRateEventBus,
    private val autoConnectBleDevice: AutoConnectBleDevice,
    private val specificationsRepo: SpecificationsRepo,
    private val alarmReasonRepo: AlarmReasonRepo
) : ViewModel() {

    val monitoring: LiveData<Boolean>
        get() = sleepMonitor.monitoring

    val startAlarm = SingleLiveEvent<AlarmReason>()

    val heartRate: LiveData<Int>
        get() = _heartRate

    val heartRateStatus: LiveData<MonitorParameterStatus>
        get() = sleepMonitor.heartRateStatus

    val eyesClosureStatus: LiveData<MonitorParameterStatus>
        get() = sleepMonitor.eyesClosureStatus

    private val _heartRate = MutableLiveData<Int>()
    private var detectionInProgress = false
    private var heartRateListen: Job? = null

    private val alarmObserver = { reason: AlarmReason ->
        if (monitoring.value!!) {
            alarmReasonRepo.insertLastAlarmReason(reason)
            startAlarm.value = reason
            onStopMonitor()
        }
    }

    init {
        listenForHeartRate()
        sleepMonitor.alarm
            .observeForever(alarmObserver)
    }

    override fun onCleared() {
        sleepMonitor.alarm.removeObserver(alarmObserver)
        heartRateListen?.cancel()
        super.onCleared()
    }

    fun onUnnecessaryAlarmResponse() {
        when (alarmReasonRepo.fetchLastAlarmReason()) {
            EYES_CLOSURE -> updateEyesClosureSpecification()
            HEART_RATE -> updateHeartRateSpecification()
            null -> {
            }
        }
    }

    private fun updateEyesClosureSpecification() {
        specificationsRepo.fetchEyesDataSpecification()
            ?.also { spec ->
                var newSpec: EyesDataSpecification? = null

                if (spec.timeThreshold < Const.EYES_CLOSURE_MAX_TIME_THRESHOLD) {
                    newSpec = spec.copy(timeThreshold = spec.timeThreshold + 500L)
                }

                if (spec.valueThreshold > Const.EYES_CLOSURE_MIN_VALUE_THRESHOLD) {
                    newSpec
                        ?.also {
                            newSpec = it.copy(valueThreshold = spec.valueThreshold - 0.02f)
                        }
                        ?: run {
                            newSpec = spec.copy(valueThreshold = spec.valueThreshold - 0.02f)
                        }
                }

                newSpec?.let { specificationsRepo.insertOrUpdate(it) }
            } ?: specificationsRepo.insertOrUpdate(EyesDataSpecification())
    }

    private fun updateHeartRateSpecification() {
        specificationsRepo.fetchHeartRateSpecification()
            ?.also { spec ->
                var newSpec: HeartRateSpecification? = null

                if (spec.timeThreshold < Const.HEAR_RATE_MAX_TIME_THRESHOLD) {
                    newSpec = spec.copy(timeThreshold = spec.timeThreshold + 500L)
                }

                if (spec.valueThreshold > Const.HEAR_RATE_MIN_VALUE_THRESHOLD) {
                    newSpec
                        ?.also {
                            newSpec = it.copy(valueThreshold = spec.valueThreshold - 1)
                        }
                        ?: run {
                            newSpec = spec.copy(valueThreshold = spec.valueThreshold - 1)
                        }
                }

                newSpec?.let { specificationsRepo.insertOrUpdate(it) }
            } ?: specificationsRepo.insertOrUpdate(HeartRateSpecification())
    }

    fun onStartMonitor() {
        sleepMonitor.startMonitoring()
    }

    fun onStopMonitor() {
        sleepMonitor.stopMonitoring()
    }

    fun getFrameProcessor(): (frame: Frame) -> Unit =
        { frame ->
            if (!detectionInProgress) {
                detectionInProgress = true

                Log.d(Const.FACE_RECOGNITION_TAG, "Rotation: ${frame.rotation}")
                val metadata = getMetadata(frame)
                val realTimeOpts = getRealTimeOpts()

                val image = FirebaseVisionImage.fromByteArray(frame.data, metadata)
                val detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts)

                detector.detectInImage(image)
                    .addOnSuccessListener { result ->
                        result.forEach { face ->
                            if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY &&
                                face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY
                            ) {
                                // swap done intentionally because of the front camera
                                val leftEyeProb = face.rightEyeOpenProbability
                                val rightEyeProb = face.leftEyeOpenProbability

                                eyesMeasurementEventBus.send(
                                    EyesMeasurement(
                                        leftOpenProbability = leftEyeProb,
                                        rightOpenProbability = rightEyeProb
                                    )
                                )
                                Log.d(
                                    Const.FACE_RECOGNITION_TAG,
                                    "Eyes prob: left -> $leftEyeProb   right -> $rightEyeProb"
                                )
                            }
                        }
                        detectionInProgress = false
                    }
                    .addOnFailureListener { failure ->
                        Log.e(Const.FACE_RECOGNITION_TAG, "Failure", failure)
                    }
            }
        }

    fun reconnectDevice() {
        autoConnectBleDevice()
    }

    private fun listenForHeartRate() {
        heartRateListen = MainScope().launch {
            heartRateEventBus.listen()
                .openSubscription()
                .consumeEach {
                    _heartRate.postValue(it)
                }
        }
    }

    private fun getRealTimeOpts(): FirebaseVisionFaceDetectorOptions =
        FirebaseVisionFaceDetectorOptions.Builder()
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

    private fun getMetadata(frame: Frame): FirebaseVisionImageMetadata =
        FirebaseVisionImageMetadata.Builder()
            .setWidth(frame.size.width)
            .setHeight(frame.size.height)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(frame.visionImageRotation())
            .build()
}
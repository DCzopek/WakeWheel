package com.example.wakewheel.monitoring

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wakewheel.Const
import com.example.wakewheel.extensions.visionImageRotation
import com.example.wakewheel.receivers.EyesMeasurementEventBus
import com.example.wakewheel.utils.SingleLiveEvent
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.Frame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MonitoringViewModel @Inject constructor(
    private val sleepMonitor: SleepMonitor,
    private val eyesMeasurementEventBus: EyesMeasurementEventBus
) : ViewModel() {

    private var detectionInProgress = false
    private val alarmObserver = { alarm: Boolean ->
        if (alarm) {
            startAlarm.postValue(Any())
        }
    }

    val message: LiveData<String>
        get() = _message

    private val _message = MutableLiveData<String>()

    val startAlarm = SingleLiveEvent<Any>()

    init {
        sleepMonitor.alarm
            .observeForever(alarmObserver)
    }

    override fun onCleared() {
        sleepMonitor.alarm
            .removeObserver(alarmObserver)
        super.onCleared()
    }

    fun onStartMonitor() {
        sleepMonitor.startMonitoring()
    }

    fun stopMonitor() {
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
                                if (leftEyeProb > 0.8 && rightEyeProb > 0.8) {
                                    _message.postValue("Your eyes are open")
                                } else if (leftEyeProb < 0.2 && rightEyeProb < 0.2) {
                                    _message.postValue("Your eyes are closed")
                                } else {
                                    _message.postValue("You have at least one eye closed")
                                }
                            }
                        }
                        detectionInProgress = false
                    }
                    .addOnFailureListener { failure ->
                        Log.e(Const.FACE_RECOGNITION_TAG, "Failure", failure)
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
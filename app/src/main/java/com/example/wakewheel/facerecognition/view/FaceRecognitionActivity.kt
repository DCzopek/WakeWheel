package com.example.wakewheel.facerecognition.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.wakewheel.Const.FACE_RECOGNITION_TAG
import com.example.wakewheel.R
import com.example.wakewheel.extensions.visionImageRotation
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.Frame
import kotlinx.android.synthetic.main.activity_face_recognition.camera
import kotlinx.android.synthetic.main.activity_face_recognition.text

class FaceRecognitionActivity : AppCompatActivity() {

    var detectionInProgress = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)

        camera.setLifecycleOwner { this.lifecycle }
        camera.addFrameProcessor { frame: Frame ->

            if (detectionInProgress) {
                return@addFrameProcessor
            }

            detectionInProgress = true
            Log.d(FACE_RECOGNITION_TAG, "Rotation: ${frame.rotation}")
            val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(frame.size.width)
                .setHeight(frame.size.height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(frame.visionImageRotation())
                .build()

            val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

            val image = FirebaseVisionImage.fromByteArray(frame.data, metadata)
            val detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts)

            val result = detector.detectInImage(image)
                .addOnSuccessListener { result ->
                    result.forEach { face ->
                        text.text = ""
                        if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY &&
                            face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY
                        ) {
                            val leftEyeProb = face.leftEyeOpenProbability
                            val rightEyeProb = face.rightEyeOpenProbability
                            Log.d(
                                FACE_RECOGNITION_TAG,
                                "Eyes prob: left -> $leftEyeProb   right -> $rightEyeProb"
                            )
                            if (leftEyeProb > 0.8 && rightEyeProb > 0.8) {
                                text.text = "Your eyes are open"
                            } else {
                                text.text = "You have at least one eye closed"
                            }
                        }
                    }
                    detectionInProgress = false
                }
                .addOnFailureListener { failure ->
                    Log.e(FACE_RECOGNITION_TAG, "Failure", failure)
                }
        }
    }
}

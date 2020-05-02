package com.example.wakewheel.extensions

import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.Frame

fun Frame.visionImageRotation() = when (this.rotation) {
    90 -> FirebaseVisionImageMetadata.ROTATION_90
    180 -> FirebaseVisionImageMetadata.ROTATION_180
    270 -> FirebaseVisionImageMetadata.ROTATION_270
    else -> FirebaseVisionImageMetadata.ROTATION_0
}
package com.example.wakewheel

import com.example.wakewheel.heartrate.GattAttributes
import java.util.UUID

object Const {
    val UUID_HEART_RATE_MEASUREMENT: UUID = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT)

    const val BLUETOOTH_CONNECTION_TIMEOUT = 30_000L
    const val HEAR_RATE_MIN_VALUE_THRESHOLD = 45
    const val HEAR_RATE_MAX_TIME_THRESHOLD = 20_000L
    const val EYES_CLOSURE_MIN_VALUE_THRESHOLD = 0.15f
    const val EYES_CLOSURE_MAX_TIME_THRESHOLD = 10_000L

    const val FIRST_USE_KEY = "wakewheel.firstUse"

    const val FACE_RECOGNITION_TAG = "FaceRecognitionScreen"

    const val EXTRA_HEART_RATE_DATA = "com.example.wakewheel.EXTRA_HEART_RATE_DATA"

    const val STATE_CONNECTED = 2

    const val ACTION_DATA_AVAILABLE = "com.example.wakewheel.ACTION_DATA_AVAILABLE"
    const val ACTION_GATT_CONNECTED = "com.example.wakewheel.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED = "com.example.wakewheel.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED =
        "com.example.wakewheel.ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED"
}
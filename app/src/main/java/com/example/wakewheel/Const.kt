package com.example.wakewheel

import com.example.wakewheel.heartrate.GattAttributes
import java.util.UUID

object Const {
    const val STATE_DISCONNECTED = 0
    const val STATE_CONNECTING = 1
    const val STATE_CONNECTED = 2
    const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
    const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    val UUID_HEART_RATE_MEASUREMENT: UUID = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT)
    const val FACE_RECOGNITION_TAG = "FaceRecognitionScreen"
}
package com.example.wakewheel.heartrate.services

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.wakewheel.Const
import com.example.wakewheel.heartrate.GattAttributes
import com.example.wakewheel.heartrate.receivers.BluetoothGattAction
import com.example.wakewheel.heartrate.receivers.BluetoothGattAction.CONNECT_TO_HEART_RATE_DEVICE_SUCCEED
import com.example.wakewheel.heartrate.receivers.BluetoothGattAction.SET_NOTIFICATION_FAILS
import com.example.wakewheel.heartrate.receivers.BluetoothGattAction.SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE
import com.example.wakewheel.heartrate.receivers.BluetoothGattEventBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.UUID

@ExperimentalCoroutinesApi
@SuppressLint("Registered")
class BluetoothLeService(
    private val context: Context,
    private val bluetoothLeServiceBinder: BluetoothLeServiceBinder,
    private val gattEventBus: BluetoothGattEventBus
) : Service() {

    private var connectionState = BluetoothAdapter.STATE_DISCONNECTED
    private var connectedDevice: BluetoothGatt? = null
    private var eventBusListener: Job? = null

    override fun onCreate() {
        super.onCreate()
        eventBusListener = MainScope().launch {
            gattEventBus.listen()
                .openSubscription()
                .consumeEach {
                    if (it == BluetoothGattAction.GATT_DISCONNECTED) {
                        connectedDevice = null
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventBusListener?.cancel()
    }

    override fun onBind(intent: Intent): IBinder =
        bluetoothLeServiceBinder

    fun connectDevice(device: BluetoothDevice) {
        connectedDevice?.setNotification(enable = false)
        connectedDevice?.disconnect()
        connectedDevice = device.connectGatt(this, true, gattCallback)
    }

    fun connectToHeartRateService() {
        with(gattEventBus) {
            connectedDevice?.setNotification(enable = true)
                ?.let { success ->
                    if (success) send(CONNECT_TO_HEART_RATE_DEVICE_SUCCEED)
                    else send(SET_NOTIFICATION_FAILS)
                } ?: send(SET_NOTIFICATION_FAILS_NO_CONNECTED_DEVICE)
        }
    }

    fun connectedDevice(): BluetoothDevice? =
        connectedDevice?.device

    private fun BluetoothGatt.setNotification(enable: Boolean): Boolean =
        getHeartRateCharacteristics()
            ?.let {
                setCharacteristicNotification(it, enable) &&
                    writeDescriptor(it.getEnableNotificationDescriptor())
            } ?: false

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = Const.ACTION_GATT_CONNECTED
                    connectionState = Const.STATE_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i("BlueService", "Connected to GATT server.")
                    Log.i(
                        "BlueService", "Attempting to start service discovery: " +
                            gatt.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = Const.ACTION_GATT_DISCONNECTED
                    connectionState = BluetoothAdapter.STATE_DISCONNECTED
                    Log.i("BlueService", "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        private fun broadcastUpdate(action: String) {
            val intent = Intent(action)
            context.sendBroadcast(intent)
        }

        private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
            val intent = Intent(action)

            when (characteristic.uuid) {
                Const.UUID_HEART_RATE_MEASUREMENT -> {
                    val flag = characteristic.properties
                    val format = when (flag and 0x01) {
                        0x01 -> {
                            Log.d("BlueService", "Heart rate format UINT16.")
                            BluetoothGattCharacteristic.FORMAT_UINT16
                        }
                        else -> {
                            Log.d("BlueService", "Heart rate format UINT8.")
                            BluetoothGattCharacteristic.FORMAT_UINT8
                        }
                    }
                    val heartRate = characteristic.getIntValue(format, 1)
                    Log.d("BlueService", String.format("Received heart rate: %d", heartRate))
                    intent.putExtra(Const.EXTRA_HEART_RATE_DATA, heartRate)
                }
                else -> {
                    val data: ByteArray? = characteristic.value
                    if (data?.isNotEmpty() == true) {
                        try {
                            val intFromHex: Int = data.joinToString(separator = " ") {
                                String.format("%02X", it)
                            }.toInt()
                            intent.putExtra(Const.EXTRA_HEART_RATE_DATA, intFromHex)
                        } catch (ignored: Exception) {
                        }

                    }
                }

            }
            context.sendBroadcast(intent)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && hasHeartRateService(gatt)) {
                broadcastUpdate(Const.ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            broadcastUpdate(Const.ACTION_DATA_AVAILABLE, characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Const.ACTION_DATA_AVAILABLE, characteristic)
            }
        }
    }

    private fun BluetoothGatt.getHeartRateCharacteristics(): BluetoothGattCharacteristic? =
        this.services
            ?.firstOrNull { it.uuid == UUID.fromString(GattAttributes.HEART_RATE_SERVICE) }
            ?.let { gattService ->
                gattService.characteristics
                    ?.firstOrNull { it.uuid == UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT) }
            }

    private fun BluetoothGattCharacteristic.getEnableNotificationDescriptor(): BluetoothGattDescriptor =
        this.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
            .apply { value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE }

    private fun hasHeartRateService(gatt: BluetoothGatt): Boolean =
        gatt.services
            .firstOrNull { it.uuid == UUID.fromString(GattAttributes.HEART_RATE_SERVICE) }
            ?.let { true }
            ?: false
}
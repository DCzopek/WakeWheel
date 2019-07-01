package com.example.wakewheel

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.wakewheel.services.BluetoothLeServiceBinder

@SuppressLint("Registered")
class BluetoothLeService(
    private val context: Context,
    private val bluetoothLeServiceBinder: BluetoothLeServiceBinder
) : Service() {

    lateinit var bluetoothGatt: BluetoothGatt
    override fun onBind(intent: Intent): IBinder =
        bluetoothLeServiceBinder

    private var connectionState = BluetoothAdapter.STATE_DISCONNECTED

    val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = Consts.ACTION_GATT_CONNECTED
                    connectionState = Consts.STATE_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i("BlueService", "Connected to GATT server.")
                    Log.i(
                        "BlueService", "Attempting to start service discovery: " +
                                gatt.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = Consts.ACTION_GATT_DISCONNECTED
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
                Consts.UUID_HEART_RATE_MEASUREMENT -> {
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
                    intent.putExtra(Consts.EXTRA_DATA, (heartRate).toString())
                }
                else -> {
                    val data: ByteArray? = characteristic.value
                    if (data?.isNotEmpty() == true) {
                        val hexString: String = data.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        intent.putExtra(Consts.EXTRA_DATA, "$data\n$hexString")
                    }
                }

            }
            context.sendBroadcast(intent)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(Consts.ACTION_GATT_SERVICES_DISCOVERED)
                else -> Log.w("BlueService", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            broadcastUpdate(Consts.ACTION_DATA_AVAILABLE, characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(Consts.ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }
    }
}
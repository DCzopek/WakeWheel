package com.example.wakewheel.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.AutoConnectBleDevice
import com.example.wakewheel.receivers.gatt.BluetoothGattReceiver
import dagger.android.AndroidInjection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RuntimePermissions
class MainActivity : AppCompatActivity() {

    @Inject lateinit var autoConnectBleDevice: AutoConnectBleDevice

    private val bleReceiver = BluetoothGattReceiver()
    private var backPressListener: BackPressListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        setContentView(R.layout.activity_main)
        registerReceiver(bleReceiver, BluetoothGattReceiver.intentFilter)
        autoConnectBleDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bleReceiver)
    }

    fun attachBackPressListener(listener: BackPressListener) {
        backPressListener = listener
    }

    fun detachBackPressListener() {
        backPressListener = null
    }

    override fun onBackPressed() {
        backPressListener?.onBackPress()
            ?: super.onBackPressed()
    }
}

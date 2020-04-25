package com.example.wakewheel.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wakewheel.R
import com.example.wakewheel.receivers.gatt.BluetoothGattReceiver
import dagger.android.AndroidInjection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import permissions.dispatcher.RuntimePermissions

@ExperimentalCoroutinesApi
@RuntimePermissions
class MainActivity : AppCompatActivity() {

    private val bleReceiver = BluetoothGattReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        setContentView(R.layout.activity_main)
        registerReceiver(bleReceiver, BluetoothGattReceiver.intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bleReceiver)
    }
}

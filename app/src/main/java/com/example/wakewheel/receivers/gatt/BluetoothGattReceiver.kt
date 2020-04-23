package com.example.wakewheel.receivers.gatt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.wakewheel.Const
import dagger.android.AndroidInjection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class BluetoothGattReceiver : BroadcastReceiver() {

    @Inject lateinit var controller: BluetoothGattController

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        intent?.run {
            MainScope().launch {
                when (action) {
                    Const.ACTION_DATA_AVAILABLE -> controller.onDataReceive(getStringExtra(Const.EXTRA_HEART_RATE_DATA))
                    Const.ACTION_GATT_CONNECTED -> controller.onGattConnected()
                    Const.ACTION_GATT_DISCONNECTED -> controller.onGattDisconnected()
                    Const.ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED -> controller.onHeartServiceDiscovered()
                    else -> println("Unknown action in BluetoothGattReceiver")
                }
            }
        }
    }

    companion object {
        val intentFilter = IntentFilter(Const.ACTION_GATT_CONNECTED).apply {
            addAction(Const.ACTION_DATA_AVAILABLE)
            addAction(Const.ACTION_GATT_DISCONNECTED)
            addAction(Const.ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED)
        }
    }
}
package com.example.wakewheel.heartrate.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.wakewheel.Const
import dagger.android.AndroidInjection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class BluetoothGattReceiver : BroadcastReceiver() {

    @Inject lateinit var controller: BluetoothGattController

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        intent?.run {
            when (action) {
                Const.ACTION_DATA_AVAILABLE -> controller.onDataReceive(
                    getIntExtra(Const.EXTRA_HEART_RATE_DATA, 0)
                )
                Const.ACTION_GATT_CONNECTED -> controller.onGattConnected()
                Const.ACTION_GATT_DISCONNECTED -> controller.onGattDisconnected()
                Const.ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED -> controller.onHeartServiceDiscovered()
                else -> println("Unknown action in BluetoothGattReceiver")
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
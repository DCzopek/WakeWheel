package com.example.wakewheel.heartrate.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.GattAttributes
import com.example.wakewheel.receivers.gatt.BluetoothGattEventBus
import com.example.wakewheel.receivers.gatt.BluetoothGattReceiver
import com.example.wakewheel.services.BluetoothLeService
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_heart_rate.device_search
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HeartRateActivity : AppCompatActivity() {

    @Inject lateinit var bluetoothLeService: BluetoothLeService
    @Inject lateinit var bleHandler: BleHandler
    @Inject lateinit var eventBus: BluetoothGattEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)

        registerReceiver(BluetoothGattReceiver(), BluetoothGattReceiver.intentFilter)

        device_search.setOnClickListener {
            startActivity(Intent(this, PairBleDeviceActivity::class.java))
        }

        MainScope().launch {
            eventBus.listen()
                .openSubscription()
                .consumeEach { println(it.name) }
        }
    }

    private fun registerNotification() {
        bluetoothLeService.bluetoothGatt.services?.filter {
            it.uuid == UUID.fromString(GattAttributes.HEART_RATE_SERVICE)
        }
            ?.let { gattServices ->
                gattServices.firstOrNull()
                    .let { gattService ->
                        gattService?.characteristics
                            ?.filter {
                                it.uuid == UUID.fromString(
                                    GattAttributes.HEART_RATE_MEASUREMENT
                                )
                            }
                            .let {
                                it?.firstOrNull()
                                    ?.let { it1 -> bleHandler.setNotification(it1) }
                            }
                    }
            }
    }
}

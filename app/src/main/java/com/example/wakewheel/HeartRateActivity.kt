package com.example.wakewheel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_heart_rate.*
import java.util.UUID
import javax.inject.Inject

class HeartRateActivity : AppCompatActivity() {

    @Inject lateinit var bluetoothLeService: BluetoothLeService
    @Inject lateinit var heartRate: HeartRate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)
        AndroidInjection.inject(this)


        val filter = IntentFilter(Consts.ACTION_GATT_CONNECTED)
        filter.addAction(Consts.ACTION_DATA_AVAILABLE)
        filter.addAction(Consts.ACTION_GATT_DISCONNECTED)
        filter.addAction(Consts.ACTION_GATT_SERVICES_DISCOVERED)
        registerReceiver(gattUpdateReceiver, filter)


        device_search.setOnClickListener {
            startActivity(Intent(this, BleSearchActivity::class.java))
        }


    }

    // todo - wynieść do innej klasy
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
                                    ?.let { it1 -> heartRate.setNotification(it1) }
                            }
                    }
            }

    }
    private val gattUpdateReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context,
            intent: Intent
        ) {

            when (intent.action) {
                Consts.ACTION_GATT_CONNECTED -> {
                    println("Gatt connected!! ")
                }
                Consts.ACTION_GATT_DISCONNECTED -> {
                    println("Gatt Disconnected !")
                }
                Consts.ACTION_GATT_SERVICES_DISCOVERED -> {
                    println("Gatt Services discovered !")
                    registerNotification()
                }
                Consts.ACTION_DATA_AVAILABLE -> {
                    tv_heart_rate.text = intent.getStringExtra(Consts.EXTRA_DATA)
                }
            }
        }
    }
}
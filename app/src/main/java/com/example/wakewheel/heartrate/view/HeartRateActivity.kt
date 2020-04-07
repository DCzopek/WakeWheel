package com.example.wakewheel.heartrate.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wakewheel.Const
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.GattAttributes
import com.example.wakewheel.services.BluetoothLeService
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_heart_rate.device_search
import kotlinx.android.synthetic.main.activity_heart_rate.tv_heart_rate
import java.util.UUID
import javax.inject.Inject

class HeartRateActivity : AppCompatActivity() {

    @Inject lateinit var bluetoothLeService: BluetoothLeService
    @Inject lateinit var bleHandler: BleHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)
        println("Before injection")
        println("After injection")

        // todo wynieść ten filter razem z reciverem do innej klasy
        val filter = IntentFilter(Const.ACTION_GATT_CONNECTED)
        filter.addAction(Const.ACTION_DATA_AVAILABLE)
        filter.addAction(Const.ACTION_GATT_DISCONNECTED)
        filter.addAction(Const.ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED)
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
                                    ?.let { it1 -> bleHandler.setNotification(it1) }
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
                Const.ACTION_GATT_CONNECTED -> {
                    println("Gatt connected!! ")
                }
                Const.ACTION_GATT_DISCONNECTED -> {
                    println("Gatt Disconnected !")
                }
                Const.ACTION_GATT_HEART_RATE_SERVICE_DISCOVERED -> {
                    registerNotification()
                }
                Const.ACTION_DATA_AVAILABLE -> {
                    tv_heart_rate.text = intent.getStringExtra(Const.EXTRA_DATA)
                }
            }
        }
    }
}
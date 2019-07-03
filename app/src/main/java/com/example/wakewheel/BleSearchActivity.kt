package com.example.wakewheel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_ble_search.*
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject

class BleSearchActivity : AppCompatActivity() {

    @Inject lateinit var devicesRecyclerAdapter: DevicesRecyclerAdapter
    @Inject lateinit var heartRate: HeartRate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidInjection.inject(this)
        setContentView(R.layout.activity_ble_search)

        recyclerView.adapter = devicesRecyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, getRecyclerOrientation()))



        ble_search_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ),
                    0
                )

            } else {
                heartRate.scanForBle()
            }
        }
    }

    private fun getRecyclerOrientation() =
        (recyclerView.layoutManager as LinearLayoutManager).orientation
}




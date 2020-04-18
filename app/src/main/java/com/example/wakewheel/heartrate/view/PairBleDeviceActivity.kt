package com.example.wakewheel.heartrate.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleDevice
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_ble_search.ble_search_button
import kotlinx.android.synthetic.main.activity_ble_search.recyclerView
import javax.inject.Inject

class PairBleDeviceActivity : AppCompatActivity(), DeviceRecyclerClickListener {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var devicesRecyclerAdapter: DevicesRecyclerAdapter
    private lateinit var viewModel: PairBleDeviceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ble_search)

        viewModel = ViewModelProvider(this, viewModelFactory).get(PairBleDeviceViewModel::class.java)

        devicesRecyclerAdapter = DevicesRecyclerAdapter(this)

        recyclerView.adapter = devicesRecyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, getRecyclerOrientation()))

        ble_search_button.setOnClickListener {
            if (!permissionsGranted()) requestPermissions()
            else viewModel.onScanClick()
        }

        viewModel.deviceList
            .observe(this) { updateRecycler(it) }

        viewModel.requestBluetoothEnable
            .observe(this) {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .run { startActivityForResult(this, BLUETOOTH_ENABLE_REQUEST_CODE) }
            }
    }

    private fun updateRecycler(list: List<BleDevice>) {
        devicesRecyclerAdapter.deviceList = list
        devicesRecyclerAdapter.notifyDataSetChanged()
    }

    private fun getRecyclerOrientation() =
        (recyclerView.layoutManager as LinearLayoutManager).orientation

    private fun permissionsGranted() =
        (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ),
            0
        )
    }

    override fun onPairClicked(macAddress: String) {
        viewModel.onPairClicked(macAddress)
    }

    companion object {
        private const val BLUETOOTH_ENABLE_REQUEST_CODE = 111
    }
}

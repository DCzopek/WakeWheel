package com.example.wakewheel.heartrate.view

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleDevice
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_ble_search.ble_search_button
import kotlinx.android.synthetic.main.activity_ble_search.recyclerView
import javax.inject.Inject

class SearchBleDeviceFragment : Fragment(), DeviceRecyclerClickListener {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var devicesRecyclerAdapter: DevicesRecyclerAdapter
    private lateinit var viewModel: PairBleDeviceViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.activity_ble_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(PairBleDeviceViewModel::class.java)
        navController = findNavController()
        devicesRecyclerAdapter = DevicesRecyclerAdapter(this)

        recyclerView.adapter = devicesRecyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(activity, getRecyclerOrientation()))

        ble_search_button.setOnClickListener {
            if (!permissionsGranted()) requestPermissions()
            else viewModel.onScanClick()
        }

        viewModel.deviceList
            .observe(viewLifecycleOwner) { updateRecycler(it) }

        viewModel.requestBluetoothEnable
            .observe(viewLifecycleOwner) {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .run {
                        startActivityForResult(
                            this,
                            BLUETOOTH_ENABLE_REQUEST_CODE
                        )
                    }
            }
    }

    private fun updateRecycler(list: List<BleDevice>) {
        devicesRecyclerAdapter.deviceList = list
        devicesRecyclerAdapter.notifyDataSetChanged()
    }

    private fun getRecyclerOrientation() =
        (recyclerView.layoutManager as LinearLayoutManager).orientation

    private fun permissionsGranted() =
        (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
            == PackageManager.PERMISSION_GRANTED)

    private fun requestPermissions() {
        requestPermissions(
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
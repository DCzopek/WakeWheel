package com.example.wakewheel.heartrate.view

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleDevice
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.DURING
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.FAIL
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.NO_HEART_RATE
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.SUCCESS
import com.example.wakewheel.heartrate.view.DeviceConnectionStatus.TIMEOUT
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_ble_search.progressBar
import kotlinx.android.synthetic.main.activity_ble_search.recyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SearchBleDeviceFragment : Fragment(), DeviceRecyclerClickListener {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var devicesRecyclerAdapter: DevicesRecyclerAdapter
    private lateinit var viewModel: ManageBleDeviceViewModel

    private var firstScan = true

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
            ViewModelProvider(this, viewModelFactory).get(ManageBleDeviceViewModel::class.java)
        devicesRecyclerAdapter = DevicesRecyclerAdapter(this)

        recyclerView.adapter = devicesRecyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(DividerItemDecoration(activity, getRecyclerOrientation()))

        checkPermissions()

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

        viewModel.deviceConnection
            .observe(viewLifecycleOwner) { status ->
                when (status!!) {
                    DURING -> showSnackbar(view, R.string.device_status_during)
                    TIMEOUT -> {
                        showSnackbar(view, R.string.device_status_timeout)
                        showRecycler()
                    }
                    SUCCESS -> {
                        showSnackbar(view, R.string.device_status_success)
                        showRecycler()
                    }
                    FAIL -> {
                        showSnackbar(view, R.string.device_status_fail)
                        showRecycler()
                    }
                    NO_HEART_RATE -> {
                        showSnackbar(view, R.string.device_status_no_heart_rate)
                        showRecycler()
                    }
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            grantResults
                .firstOrNull { it == PackageManager.PERMISSION_DENIED }
                ?.let {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        showRationaleDialog()
                    } else {
                        showDeniedPermissionDialog()
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startScanning()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopScanning()
    }

    private fun checkPermissions() {
        if (!permissionsGranted()) {
            requestPermissions()
        }
    }

    private fun showSnackbar(
        view: View,
        @StringRes resId: Int
    ) =
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show()

    private fun updateRecycler(list: List<BleDevice>) {
        if (firstScan) {
            progressBar.visibility = View.GONE
            firstScan = false
        }
        devicesRecyclerAdapter.deviceList = list
        devicesRecyclerAdapter.notifyDataSetChanged()
    }

    private fun getRecyclerOrientation() =
        (recyclerView.layoutManager as LinearLayoutManager).orientation

    private fun permissionsGranted() =
        (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    private fun showRationaleDialog() {
        AlertDialog.Builder(activity)
            .setMessage(R.string.location_permissions_rationale)
            .setPositiveButton(R.string.grant) { dialog, _ ->
                dialog.dismiss()
                requestPermissions()
            }
            .setNegativeButton(R.string.deny) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .create()
            .show()
    }

    private fun showDeniedPermissionDialog() {
        AlertDialog.Builder(activity)
            .setMessage(R.string.permission_denied_message)
            .setNeutralButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .create()
            .show()
    }

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ),
            BLUETOOTH_PERMISSION_REQUEST_CODE
        )
    }

    override fun onPairClicked(macAddress: String) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        viewModel.onPairClicked(macAddress)
    }

    private fun showRecycler() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    companion object {
        private const val BLUETOOTH_ENABLE_REQUEST_CODE = 111
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 112
    }
}
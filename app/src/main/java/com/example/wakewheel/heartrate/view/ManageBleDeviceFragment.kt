package com.example.wakewheel.heartrate.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleDeviceConnectionApi
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.receivers.HeartRateEventBus
import com.example.wakewheel.services.BluetoothLeService
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_device_management.address
import kotlinx.android.synthetic.main.fragment_device_management.address_label
import kotlinx.android.synthetic.main.fragment_device_management.bluetooth
import kotlinx.android.synthetic.main.fragment_device_management.connect_paired_device
import kotlinx.android.synthetic.main.fragment_device_management.device
import kotlinx.android.synthetic.main.fragment_device_management.device_label
import kotlinx.android.synthetic.main.fragment_device_management.device_search
import kotlinx.android.synthetic.main.fragment_device_management.heart_rate
import kotlinx.android.synthetic.main.fragment_device_management.no_device
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ManageBleDeviceFragment : Fragment() {

    @Inject lateinit var bluetoothLeService: BluetoothLeService
    @Inject lateinit var bleHandler: BleHandler
    @Inject lateinit var connectionApi: BleDeviceConnectionApi

    @Inject lateinit var eventBus: HeartRateEventBus
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ManageBleDeviceViewModel
    private var heartRateListen: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_device_management, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(ManageBleDeviceViewModel::class.java)

        device_search?.setOnClickListener {
            findNavController().navigate(R.id.action_pairBleDeviceFragment_to_searchBleDeviceFragment)
        }

        connect_paired_device.setOnClickListener {
            viewModel.onConnectClicked()
        }

        viewModel.deviceConnection
            .observe(viewLifecycleOwner) {
                Snackbar.make(view, it.name, Snackbar.LENGTH_SHORT).show()
            }

        connectionApi.deviceConnection
            .observe(viewLifecycleOwner) { connected ->
                if (connected) handleDeviceConnected()
                else handleDeviceDisconnected()
            }
    }

    private fun handleDeviceConnected() {
        bluetooth.setImageDrawable(activity?.getDrawable(R.drawable.ic_bluetooth_connected))
    }

    private fun handleDeviceDisconnected() {
        heart_rate.text = getString(R.string.no_value)
        bluetooth.setImageDrawable(activity?.getDrawable(R.drawable.ic_bluetooth_disconnected))
    }

    override fun onResume() {
        super.onResume()
        listenForHeartRate()
        checkUserDevice()
    }

    override fun onPause() {
        super.onPause()
        heartRateListen?.cancel()
    }

    private fun checkUserDevice() {
        viewModel.getPairedDevice()
            ?.let {
                device.text = it.name
                address.text = it.address
            } ?: handleNoDevice()
    }

    private fun handleNoDevice() {
        device_label.visibility = View.GONE
        device.visibility = View.GONE
        address_label.visibility = View.GONE
        address.visibility = View.GONE
        no_device.visibility = View.VISIBLE
    }

    private fun listenForHeartRate() {
        heartRateListen = MainScope().launch {
            eventBus.listen()
                .openSubscription()
                .consumeEach {
                    heart_rate.text = it.toString()
                }
        }
    }
}

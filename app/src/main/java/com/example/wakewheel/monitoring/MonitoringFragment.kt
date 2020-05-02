package com.example.wakewheel.monitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.AutoConnectBleDevice
import com.example.wakewheel.heartrate.BleDeviceConnectionApi
import com.example.wakewheel.receivers.HeartRateEventBus
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_face_recognition.camera
import kotlinx.android.synthetic.main.fragment_heart_rate.bluetooth
import kotlinx.android.synthetic.main.fragment_monitoring.bpm
import kotlinx.android.synthetic.main.fragment_monitoring.face
import kotlinx.android.synthetic.main.fragment_monitoring.start_monitoring
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MonitoringFragment : Fragment() {

    @Inject lateinit var connectionApi: BleDeviceConnectionApi
    @Inject lateinit var eventBus: HeartRateEventBus
    @Inject lateinit var autoConnectBleDevice: AutoConnectBleDevice
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MonitoringViewModel
    private lateinit var navController: NavController
    private var heartRateListen: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_monitoring, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MonitoringViewModel::class.java)

        camera.setLifecycleOwner { this.lifecycle }
        camera.addFrameProcessor(viewModel.getFrameProcessor())

        viewModel.message
            .observe(viewLifecycleOwner) {
                println("EyesResult : $it")
                face.text = it
            }

        connectionApi.deviceConnection
            .observe(viewLifecycleOwner) { connected ->
                if (connected) bluetooth.setImageDrawable(activity?.getDrawable(R.drawable.ic_bluetooth_connected))
                else {
                    bpm.text = "0"
                    bluetooth.setImageDrawable(activity?.getDrawable(R.drawable.ic_bluetooth_disconnected))
                }
            }


        start_monitoring.setOnClickListener {
            viewModel.onStartMonitor()
        }

        bluetooth.setOnClickListener {
            connectionApi.deviceConnection.value
                ?.let { connected ->
                    if (!connected) {
                        autoConnectBleDevice()
                        Toast.makeText(activity, "Attempting to connect", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        listenForHeartRate()

        viewModel.startAlarm
            .observe(viewLifecycleOwner) {
                navController.navigate(R.id.action_monitoringFragment_to_alarmFragment)
            }
    }

    override fun onPause() {
        super.onPause()
        heartRateListen?.cancel()
    }

    private fun listenForHeartRate() {
        heartRateListen = MainScope().launch {
            eventBus.listen()
                .openSubscription()
                .consumeEach {
                    bpm.text = it.toString()
                }
        }
    }
}
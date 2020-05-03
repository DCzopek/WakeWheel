package com.example.wakewheel.monitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleDeviceConnectionApi
import com.example.wakewheel.monitoring.MonitorParameterStatus.DANGER
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_face_recognition.camera
import kotlinx.android.synthetic.main.fragment_heart_rate.bluetooth
import kotlinx.android.synthetic.main.fragment_monitoring.bpm
import kotlinx.android.synthetic.main.fragment_monitoring.camera_container
import kotlinx.android.synthetic.main.fragment_monitoring.start_monitoring
import kotlinx.android.synthetic.main.fragment_monitoring.stop_monitoring
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MonitoringFragment : Fragment() {

    @Inject lateinit var connectionApi: BleDeviceConnectionApi
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MonitoringViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_monitoring, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MonitoringViewModel::class.java)

        camera.setLifecycleOwner { this.lifecycle }
        camera.addFrameProcessor(viewModel.getFrameProcessor())

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
        stop_monitoring.setOnClickListener {
            viewModel.onStopMonitor()
        }

        bluetooth.setOnClickListener {
            connectionApi.deviceConnection.value
                ?.let { connected ->
                    if (!connected) {
                        viewModel.reconnectDevice()
                    }
                }
        }

        viewModel.monitoring
            .observe(viewLifecycleOwner) { monitoring ->
                if (monitoring) {
                    start_monitoring.visibility = View.GONE
                    stop_monitoring.visibility = View.VISIBLE
                } else {
                    start_monitoring.visibility = View.VISIBLE
                    stop_monitoring.visibility = View.GONE
                }
            }

        viewModel.heartRate
            .observe(viewLifecycleOwner) {
                bpm.text = it.toString()
            }

        viewModel.heartRateStatus
            .observe(viewLifecycleOwner) {
                if (it == DANGER) bpm.setBackgroundResource(R.drawable.alarm_background_nok)
                else bpm.setBackgroundResource(R.drawable.alarm_background_ok)
            }

        viewModel.eyesClosureStatus
            .observe(viewLifecycleOwner) {
                if (it == DANGER) camera_container.setBackgroundResource(R.drawable.alarm_background_nok)
                else camera_container.setBackgroundResource(R.drawable.alarm_background_ok)
            }

        navController = findNavController()
        viewModel.startAlarm
            .observe(viewLifecycleOwner) {
                navController.navigate(R.id.action_monitoringFragment_to_alarmFragment)
            }
    }
}
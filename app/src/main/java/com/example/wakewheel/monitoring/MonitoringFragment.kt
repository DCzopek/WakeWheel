package com.example.wakewheel.monitoring

import android.app.AlertDialog
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
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
import com.example.wakewheel.receivers.HeartRateEventBus
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_device_management.bluetooth
import kotlinx.android.synthetic.main.fragment_monitoring.bpm
import kotlinx.android.synthetic.main.fragment_monitoring.bpm_label
import kotlinx.android.synthetic.main.fragment_monitoring.camera
import kotlinx.android.synthetic.main.fragment_monitoring.camera_container
import kotlinx.android.synthetic.main.fragment_monitoring.heart_rate_container
import kotlinx.android.synthetic.main.fragment_monitoring.start_monitoring
import kotlinx.android.synthetic.main.fragment_monitoring.stop_monitoring
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MonitoringFragment : Fragment() {

    @Inject lateinit var connectionApi: BleDeviceConnectionApi
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var eventBus: HeartRateEventBus

    private lateinit var viewModel: MonitoringViewModel
    private lateinit var navController: NavController

    private lateinit var alarm: Ringtone

    private var audioManager: AudioManager? = null
    private var player: MediaPlayer? = null

    private var heartRateListen: Job? = null

    private var deviceConnected = false

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

        alarm = RingtoneManager.getRingtone(
            activity,
            RingtoneManager.getActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_ALARM)
        )

        audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        connectionApi.deviceConnection
            .observe(viewLifecycleOwner) { connected ->
                if (connected) handleDeviceConnected()
                else handleDeviceDisconnected()
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

        viewModel.heartRateStatus
            .observe(viewLifecycleOwner) {
                if (deviceConnected) {
                    if (it == DANGER) heart_rate_container.setBackgroundResource(R.drawable.alarm_background_nok)
                    else heart_rate_container.setBackgroundResource(R.drawable.alarm_background_ok)
                }
            }

        viewModel.eyesClosureStatus
            .observe(viewLifecycleOwner) {
                if (it == DANGER) camera_container.setBackgroundResource(R.drawable.alarm_background_nok)
                else camera_container.setBackgroundResource(R.drawable.alarm_background_ok)
            }

        navController = findNavController()

        viewModel.alarm
            .observe(viewLifecycleOwner) { alarm ->
                if (alarm) {
                    playAlarmSound()
                    showAlarmDialog()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        listenForHeartRate()
    }

    override fun onPause() {
        super.onPause()
        heartRateListen?.cancel()
    }

    override fun onDestroyView() {
        viewModel.clearAlarm()
        viewModel.alarm.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
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

    private fun showAlarmDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.alarm)
            .setMessage(getString(R.string.alarm_message))
            .setNeutralButton(R.string.dismiss) { dialog, _ ->
                player?.stop()
                viewModel.clearAlarm()
                dialog.dismiss()
                showFeedbackDialog()
            }
            .create()
            .show()
    }

    private fun handleDeviceDisconnected() {
        deviceConnected = false
        bpm_label.visibility = View.GONE
        bpm.text = getString(R.string.no_value)
        heart_rate_container.setBackgroundResource(R.drawable.alarm_background_undefined)
        bluetooth.setImageDrawable(activity?.getDrawable(R.drawable.ic_bluetooth_disconnected))
    }

    private fun handleDeviceConnected() {
        deviceConnected = true
        bpm_label.visibility = View.VISIBLE
        bluetooth.setImageDrawable(activity?.getDrawable(R.drawable.ic_bluetooth_connected))
    }

    private fun playAlarmSound() {
        player = MediaPlayer.create(
            activity,
            RingtoneManager.getActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_ALARM)
        )

        audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            ?.let { max ->
                audioManager?.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    max,
                    AudioManager.FLAG_PLAY_SOUND
                )
            }

        player?.setVolume(1f, 1f)
        player?.isLooping = true
        player?.start()
    }

    private fun showFeedbackDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.feedback_request)
            .setMessage(R.string.alarm_question)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                viewModel.onUnnecessaryAlarmResponse()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
    }
}
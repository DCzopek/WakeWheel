package com.example.wakewheel.monitoring

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
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import com.example.wakewheel.main.BackPressListener
import com.example.wakewheel.main.MainActivity
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragmetn_alarm.ok
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AlarmFragment : Fragment(), BackPressListener {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MonitoringViewModel

    private lateinit var alarm: Ringtone
    private var audioManager: AudioManager? = null

    private var player: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragmetn_alarm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MonitoringViewModel::class.java)

        alarm = RingtoneManager.getRingtone(
            activity,
            RingtoneManager.getActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_ALARM)
        )

        audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager


        ok.setOnClickListener {
            player?.stop()
            viewModel.stopMonitor()
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        playAlarmSound()
        backPressListen()
        super.onResume()
    }

    override fun onPause() {
        player?.stop()
        cancelBackPressListen()
        super.onPause()
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

    private fun backPressListen() {
        (activity as? MainActivity)?.attachBackPressListener(this)
    }

    private fun cancelBackPressListen() {
        (activity as? MainActivity)?.detachBackPressListener()
    }

    override fun onBackPress() {
    }
}

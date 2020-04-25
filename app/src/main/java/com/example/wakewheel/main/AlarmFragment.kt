package com.example.wakewheel.main

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
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import kotlinx.android.synthetic.main.fragmetn_alarm.ok

class AlarmFragment : Fragment() {

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
        super.onViewCreated(view, savedInstanceState)

        alarm = RingtoneManager.getRingtone(
            activity,
            RingtoneManager.getActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_ALARM)
        )

        audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        playAlarmSound()

        ok.setOnClickListener {
            player?.stop()
            findNavController().navigate(R.id.action_alarmFragment_to_monitoringFragment)
        }
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
}

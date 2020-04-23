package com.example.wakewheel.heartrate.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.receivers.HeartRateEventBus
import com.example.wakewheel.services.BluetoothLeService
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_heart_rate.device_search
import kotlinx.android.synthetic.main.fragment_heart_rate.receive_heart_rate
import kotlinx.android.synthetic.main.fragment_heart_rate.tv_heart_rate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PairBleDeviceFragment : Fragment() {

    @Inject lateinit var bluetoothLeService: BluetoothLeService
    @Inject lateinit var bleHandler: BleHandler
    @Inject lateinit var eventBus: HeartRateEventBus

    private lateinit var navController: NavController
    private var heartRateJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_heart_rate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        device_search.setOnClickListener {
            navController.navigate(R.id.action_pairBleDeviceFragment_to_searchBleDeviceFragment)
        }

        receive_heart_rate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) listenForHeartRate()
            else cancelHeartRate()
        }

    }

    private fun listenForHeartRate() {
        heartRateJob = MainScope().launch {
            eventBus.listen()
                .openSubscription()
                .consumeEach {
                    tv_heart_rate.text = it
                }
        }
    }

    private fun cancelHeartRate() {
        heartRateJob?.cancel()
    }
}
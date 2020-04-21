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
import com.example.wakewheel.receivers.gatt.BluetoothGattEventBus
import com.example.wakewheel.services.BluetoothLeService
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_heart_rate.device_search
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class PairBleDeviceFragment : Fragment() {

    @Inject lateinit var bluetoothLeService: BluetoothLeService
    @Inject lateinit var bleHandler: BleHandler
    @Inject lateinit var eventBus: BluetoothGattEventBus

    private lateinit var navController: NavController

    // todo rewrite HeartRateActivity to this fragment
    // todo do fragment injection then check if this works ( copy fragment injection from  proget backup )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.activity_heart_rate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        
        device_search.setOnClickListener {
            navController.navigate(R.id.action_pairBleDeviceFragment_to_searchBleDeviceFragment)
        }
    }

}
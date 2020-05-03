package com.example.wakewheel.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import kotlinx.android.synthetic.main.fragment_main.btn_heart_rate
import kotlinx.android.synthetic.main.fragment_main.monitoring

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_heart_rate.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_pairBleDeviceFragment)
        }

        monitoring.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_monitoringFragment)
        }
    }
}
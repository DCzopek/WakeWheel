package com.example.wakewheel.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import kotlinx.android.synthetic.main.fragment_monitoring.alarm

class MonitoringFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_monitoring, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarm.setOnClickListener {
            findNavController().navigate(R.id.action_monitoringFragment_to_alarmFragment)
        }
    }
}
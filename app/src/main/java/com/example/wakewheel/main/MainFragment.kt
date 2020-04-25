package com.example.wakewheel.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.R
import com.example.wakewheel.facerecognition.view.FaceRecognitionActivity
import kotlinx.android.synthetic.main.fragment_main.btn_heart_rate
import kotlinx.android.synthetic.main.fragment_main.dbTest
import kotlinx.android.synthetic.main.fragment_main.face_recognition
import kotlinx.android.synthetic.main.fragment_main.monitoring
import permissions.dispatcher.NeedsPermission

class MainFragment : Fragment() {

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        face_recognition.setOnClickListener {
            //openFaceRecognition()
        }

        btn_heart_rate.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_pairBleDeviceFragment)
        }

        monitoring.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_monitoringFragment)
        }

        dbTest.setOnClickListener {
            //startActivity(Intent(activity, DbTestActivity::class.java))
        }

    }

    @NeedsPermission(Manifest.permission.CAMERA)
    private fun openFaceRecognition() {
        startActivity(Intent(activity, FaceRecognitionActivity::class.java))
    }
}
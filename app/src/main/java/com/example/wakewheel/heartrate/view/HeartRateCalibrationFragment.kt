package com.example.wakewheel.heartrate.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.HeartRateCalibrationViewModel
import com.example.wakewheel.monitoring.HeartRateSpecification
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_calibrate_heart_rate.currentValue
import kotlinx.android.synthetic.main.fragment_calibrate_heart_rate.seekBar
import kotlinx.android.synthetic.main.fragment_calibrate_heart_rate.seekBar_value
import kotlinx.android.synthetic.main.fragment_calibrate_heart_rate.set
import javax.inject.Inject

class HeartRateCalibrationFragment : Fragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: HeartRateCalibrationViewModel
    private val defaultHeartRateSpecification = HeartRateSpecification()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_calibrate_heart_rate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(this, viewModelFactory).get(HeartRateCalibrationViewModel::class.java)

        viewModel.currentHeartRateValueThreshold
            .observe(viewLifecycleOwner)
            { currentValue.text = it.toString() }

        set.setOnClickListener {
            viewModel.setCurrentHeartRateValueThreshold(seekBar.progress.convertFromSeekBar())
        }

        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener())
    }

    override fun onResume() {
        super.onResume()
        seekBar.progress = defaultHeartRateSpecification.valueThreshold.convertToSeekBar()
    }

    private fun onSeekBarChangeListener() =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar_value.text = progress.convertFromSeekBar().toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        }

    private fun Int.convertFromSeekBar(): Int =
        SEEK_BAR_MIN_VALUE + this

    private fun Int.convertToSeekBar(): Int =
        this - SEEK_BAR_MIN_VALUE

    companion object {
        private const val SEEK_BAR_MIN_VALUE = 50
    }
}
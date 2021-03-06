package com.example.wakewheel.heartrate.view

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_device.view.mac_address
import kotlinx.android.synthetic.main.row_device.view.pair
import kotlinx.android.synthetic.main.row_device.view.tv_device_name

class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val deviceName: TextView = itemView.tv_device_name
    val deviceAddress: TextView = itemView.mac_address
    val pair: Button = itemView.pair
}
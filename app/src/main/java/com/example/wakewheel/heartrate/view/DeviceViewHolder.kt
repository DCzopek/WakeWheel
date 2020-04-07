package com.example.wakewheel.heartrate.view

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_device.view.*

class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val deviceName: TextView = itemView.tv_device_name
    val deviceAddress: TextView = itemView.tv_mac_address
    val connectButton: Button = itemView.btn_connect
}
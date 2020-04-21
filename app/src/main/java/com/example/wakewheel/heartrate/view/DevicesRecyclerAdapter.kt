package com.example.wakewheel.heartrate.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wakewheel.R
import com.example.wakewheel.heartrate.BleDevice

class DevicesRecyclerAdapter(private val clickListener: DeviceRecyclerClickListener) :
    RecyclerView.Adapter<DeviceViewHolder>() {

    var deviceList = listOf<BleDevice>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceViewHolder {
        return DeviceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_device,
                parent,
                false
            )
        )
    }

    override fun getItemCount() =
        deviceList.count()

    override fun onBindViewHolder(
        holder: DeviceViewHolder,
        position: Int
    ) {
        holder.deviceName.text = deviceList[position].name
        holder.deviceAddress.text = deviceList[position].macAddress

        holder.pair.setOnClickListener {
            clickListener.onPairClicked(holder.deviceAddress.text.toString())
            this.notifyDataSetChanged()
        }
    }
}
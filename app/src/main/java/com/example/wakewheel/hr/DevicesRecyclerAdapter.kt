package com.example.wakewheel.hr

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wakewheel.R
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class DevicesRecyclerAdapter : RecyclerView.Adapter<DeviceViewHolder>() {

    private val relay : PublishRelay<String> = PublishRelay.create()

    fun emit(data : String){
        relay.accept(data)
    }

    fun listen() : Observable<String>? {
        return relay.distinct()
    }

    val deviceList = arrayListOf<BleDevice>()

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

        holder.connectButton.setOnClickListener {
            emit(holder.deviceAddress.text.toString())
            this.notifyDataSetChanged()
        }
    }


}
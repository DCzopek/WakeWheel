package com.example.wakewheel.di

import android.content.Context
import com.example.wakewheel.hr.BluetoothLeService
import com.example.wakewheel.hr.DevicesRecyclerAdapter
import com.example.wakewheel.hr.HeartRate
import com.example.wakewheel.services.BluetoothLeServiceBinder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BluetoothModule {

    @Singleton
    @Provides
    fun provideHeartRate(
        context: Context,
        service: BluetoothLeService,
        recyclerAdapter: DevicesRecyclerAdapter
    ) =
        HeartRate(context, service, recyclerAdapter)

    @Singleton
    @Provides
    fun provideBluetoothLeService(
        context: Context,
        serviceBinder: BluetoothLeServiceBinder

    ): BluetoothLeService =
        BluetoothLeService(context, serviceBinder)

    @Provides
    fun provideBluetoothLeServiceBinder() =
        BluetoothLeServiceBinder()
}
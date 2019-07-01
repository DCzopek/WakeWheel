package com.example.wakewheel.di

import android.content.Context
import com.example.wakewheel.BluetoothLeService
import com.example.wakewheel.HeartRate
import com.example.wakewheel.services.BluetoothLeServiceBinder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BluetoothModule {

    @Provides
    fun provideHeartRate(
        context: Context,
        service: BluetoothLeService
    ) =
        HeartRate(context, service)

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
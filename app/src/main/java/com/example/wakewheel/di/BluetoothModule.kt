package com.example.wakewheel.di

import android.content.Context
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.receivers.gatt.BluetoothGattController
import com.example.wakewheel.receivers.gatt.BluetoothGattEventBus
import com.example.wakewheel.services.BluetoothLeService
import com.example.wakewheel.services.BluetoothLeServiceBinder
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
class BluetoothModule {

    @Singleton
    @Provides
    fun provideBleHandler(
        context: Context,
        service: BluetoothLeService
    ) =
        BleHandler(context, service)

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

    @Provides
    fun provideBluetoothGattController(eventBus: BluetoothGattEventBus) =
        BluetoothGattController(eventBus)

    @Singleton
    @Provides
    fun provideBluetoothGattEventBus() =
        BluetoothGattEventBus()
}

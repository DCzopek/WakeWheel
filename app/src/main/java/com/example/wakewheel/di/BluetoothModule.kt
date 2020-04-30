package com.example.wakewheel.di

import android.content.Context
import com.example.wakewheel.data.device.BluetoothDeviceMapper
import com.example.wakewheel.data.device.RealmBluetoothDeviceRepo
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.BluetoothDeviceRepo
import com.example.wakewheel.receivers.HeartRateEventBus
import com.example.wakewheel.receivers.gatt.BluetoothGattController
import com.example.wakewheel.receivers.gatt.BluetoothGattEventBus
import com.example.wakewheel.services.BluetoothLeService
import com.example.wakewheel.services.BluetoothLeServiceBinder
import dagger.Module
import dagger.Provides
import io.realm.Realm
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
        serviceBinder: BluetoothLeServiceBinder,
        gattEventBus: BluetoothGattEventBus
    ): BluetoothLeService =
        BluetoothLeService(
            context,
            serviceBinder,
            gattEventBus
        )

    @Provides
    fun provideBluetoothLeServiceBinder() =
        BluetoothLeServiceBinder()

    @Provides
    fun provideBluetoothGattController(
        gattEventBus: BluetoothGattEventBus,
        heartRateEventBus: HeartRateEventBus
    ) =
        BluetoothGattController(gattEventBus, heartRateEventBus)

    @Singleton
    @Provides
    fun provideBluetoothGattEventBus() =
        BluetoothGattEventBus()

    @Singleton
    @Provides
    fun provideHeartRateEventBus() =
        HeartRateEventBus()

    @Provides
    fun provideBluetoothDeviceMapper() =
        BluetoothDeviceMapper()

    @Provides
    fun provideBluetoothDeviceRepo(
        realm: Realm,
        mapper: BluetoothDeviceMapper
    ): BluetoothDeviceRepo =
        RealmBluetoothDeviceRepo(realm, mapper)
}

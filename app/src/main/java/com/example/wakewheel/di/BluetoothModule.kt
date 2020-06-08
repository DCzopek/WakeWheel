package com.example.wakewheel.di

import android.content.Context
import com.example.wakewheel.data.RealmApi
import com.example.wakewheel.data.device.BluetoothDeviceMapper
import com.example.wakewheel.data.device.RealmBluetoothDeviceRepo
import com.example.wakewheel.heartrate.AutoConnectBleDevice
import com.example.wakewheel.heartrate.BleDeviceConnectionApi
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.BluetoothDeviceConnectionObserver
import com.example.wakewheel.heartrate.BluetoothDeviceRepo
import com.example.wakewheel.heartrate.ConnectBleDevice
import com.example.wakewheel.heartrate.HeartRateEventBus
import com.example.wakewheel.heartrate.InMemoryBleDeviceConnectionRepo
import com.example.wakewheel.heartrate.receivers.BluetoothGattController
import com.example.wakewheel.heartrate.receivers.BluetoothGattEventBus
import com.example.wakewheel.heartrate.services.BluetoothLeService
import com.example.wakewheel.heartrate.services.BluetoothLeServiceBinder
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
        heartRateEventBus: HeartRateEventBus,
        connectionObserver: BluetoothDeviceConnectionObserver
    ) =
        BluetoothGattController(
            gattEventBus,
            heartRateEventBus,
            connectionObserver
        )

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
        realmApi: RealmApi,
        mapper: BluetoothDeviceMapper
    ): BluetoothDeviceRepo =
        RealmBluetoothDeviceRepo(realmApi, mapper)

    @Singleton
    @Provides
    fun provideBleDeviceConnectionRepo(): BleDeviceConnectionApi =
        InMemoryBleDeviceConnectionRepo()

    @Provides
    fun provideConnectBleDevice(
        bleHandler: BleHandler,
        gattEventBus: BluetoothGattEventBus
    ) =
        ConnectBleDevice(bleHandler, gattEventBus)

    @Provides
    fun provideAutoConnectBleDevice(
        connectBleDevice: ConnectBleDevice,
        heartRateEventBus: HeartRateEventBus,
        deviceRepo: BluetoothDeviceRepo,
        connectionApi: BleDeviceConnectionApi
    ) =
        AutoConnectBleDevice(connectBleDevice, heartRateEventBus, deviceRepo, connectionApi)

    @Singleton
    @Provides
    fun provideBluetoothDeviceConnectionObserver(api: BleDeviceConnectionApi) =
        BluetoothDeviceConnectionObserver(api)
}

package com.example.wakewheel.di

import android.content.Context
import com.example.wakewheel.data.device.BluetoothDeviceMapper
import com.example.wakewheel.data.device.RealmBluetoothDeviceRepo
import com.example.wakewheel.heartrate.AutoConnectBleDeviceOnStart
import com.example.wakewheel.heartrate.BleDeviceConnectionApi
import com.example.wakewheel.heartrate.BleHandler
import com.example.wakewheel.heartrate.BluetoothDeviceConnectionObserver
import com.example.wakewheel.heartrate.BluetoothDeviceRepo
import com.example.wakewheel.heartrate.ConnectBleDevice
import com.example.wakewheel.heartrate.InMemoryBleDeviceConnectionRepo
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
        heartRateEventBus: HeartRateEventBus,
        connectionObserver: BluetoothDeviceConnectionObserver
    ) =
        BluetoothGattController(gattEventBus, heartRateEventBus, connectionObserver)

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
    fun provideAutoConnectBleDeviceOnStart(
        connectBleDevice: ConnectBleDevice,
        heartRateEventBus: HeartRateEventBus,
        deviceRepo: BluetoothDeviceRepo,
        connectionApi: BleDeviceConnectionApi
    ) =
        AutoConnectBleDeviceOnStart(connectBleDevice, heartRateEventBus, deviceRepo, connectionApi)

    @Singleton
    @Provides
    fun provideBluetoothDeviceConnectionObserver(api: BleDeviceConnectionApi) =
        BluetoothDeviceConnectionObserver(api)
}

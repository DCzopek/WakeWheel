package com.example.wakewheel.di

import com.example.wakewheel.heartrate.receivers.BluetoothGattReceiver
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector

@Module(includes = [AndroidInjectionModule::class])
abstract class BroadcastBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun bluetoothGattReceiverInjector(): BluetoothGattReceiver
}
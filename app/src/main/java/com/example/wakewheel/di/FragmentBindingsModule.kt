package com.example.wakewheel.di

import com.example.wakewheel.heartrate.view.PairBleDeviceFragment
import com.example.wakewheel.heartrate.view.SearchBleDeviceFragment
import com.example.wakewheel.monitoring.AlarmFragment
import com.example.wakewheel.monitoring.MonitoringFragment
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module(includes = [AndroidInjectionModule::class])
abstract class FragmentBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun pairBleDeviceFragmentInjector(): PairBleDeviceFragment

    @ContributesAndroidInjector
    internal abstract fun searchBleDeviceFragmentInjector(): SearchBleDeviceFragment

    @ContributesAndroidInjector
    internal abstract fun monitoringFragmentInjector(): MonitoringFragment

    @ContributesAndroidInjector
    internal abstract fun alarmFragmentInjector(): AlarmFragment
}
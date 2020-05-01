package com.example.wakewheel.di

import com.example.wakewheel.monitoring.SleepMonitor
import com.example.wakewheel.receivers.HeartRateEventBus
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
class MonitoringModule {

    @Singleton
    @Provides
    fun provideSleepMonitor(heartRateEventBus: HeartRateEventBus) =
        SleepMonitor(heartRateEventBus)
}
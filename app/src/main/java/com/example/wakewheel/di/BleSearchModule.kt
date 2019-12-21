package com.example.wakewheel.di

import com.example.wakewheel.hr.DevicesRecyclerAdapter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BleSearchModule {

    @Singleton @Provides fun provideDevicesRecyclerAdapter() =
        DevicesRecyclerAdapter()
}
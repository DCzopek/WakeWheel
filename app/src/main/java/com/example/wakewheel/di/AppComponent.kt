package com.example.wakewheel.di

import com.example.wakewheel.App
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        AppModule::class,
        BluetoothModule::class,
        ActivityBindingsModule::class,
        BroadcastBindingsModule::class,
        FragmentBindingsModule::class,
        ViewModelModule::class,
        MonitoringModule::class
    ]
)
interface AppComponent {

    fun inject(app: App)
}
package com.example.wakewheel.di

import com.example.wakewheel.App
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        BluetoothModule::class,
        ActivityBindingsModule::class,
        ViewModelModule::class
    ]
)
interface AppComponent {

    fun inject(app: App)
}
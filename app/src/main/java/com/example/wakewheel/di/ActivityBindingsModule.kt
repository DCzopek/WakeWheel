package com.example.wakewheel.di

import com.example.wakewheel.BleSearchActivity
import com.example.wakewheel.HeartRateActivity
import com.example.wakewheel.MainActivity
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector

@Module(includes = [AndroidInjectionModule::class])
abstract class ActivityBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun mainActivityInjector(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun heartRateActivityInjector(): HeartRateActivity

    @ContributesAndroidInjector
    internal abstract fun bleSearchActivity(): BleSearchActivity
}
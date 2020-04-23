package com.example.wakewheel.di

import com.example.wakewheel.main.MainActivity
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module(includes = [AndroidInjectionModule::class])
abstract class ActivityBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun mainActivityInjector(): MainActivity
}
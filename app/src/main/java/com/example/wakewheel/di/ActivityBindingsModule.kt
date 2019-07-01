package com.example.wakewheel.di

import com.example.wakewheel.MainActivity
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector


@Module(includes = [AndroidInjectionModule::class])
abstract class ActivityBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun mainActivityInjector(): MainActivity

}
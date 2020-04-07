package com.example.wakewheel.di

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppModule (
    val context: Context
){

    @Provides fun provideContext ()  =
        this.context
}

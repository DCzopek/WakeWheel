package com.example.wakewheel.di

import android.content.Context
import com.example.wakewheel.data.RealmApi
import dagger.Module
import dagger.Provides

@Module
class AppModule(
    val context: Context
) {

    @Provides fun provideContext() =
        this.context

    @Provides fun provideRealmApi(): RealmApi =
        RealmApi()
}

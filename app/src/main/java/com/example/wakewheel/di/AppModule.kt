package com.example.wakewheel.di

import android.content.Context
import dagger.Module
import dagger.Provides
import io.realm.Realm

@Module
class AppModule(
    val context: Context
) {

    @Provides fun provideContext() =
        this.context

    @Provides fun provideRealm(): Realm =
        Realm.getDefaultInstance()
}

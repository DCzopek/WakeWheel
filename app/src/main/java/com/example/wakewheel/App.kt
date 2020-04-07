package com.example.wakewheel

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import com.example.wakewheel.di.AppModule
import com.example.wakewheel.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class App : Application(),
    HasBroadcastReceiverInjector,
    HasActivityInjector,
    HasServiceInjector {

    @Inject
    lateinit var receiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
            .inject(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(getDefaultDbConfig())
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return receiverInjector
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return activityInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return serviceInjector
    }

    private fun getDefaultDbConfig(): RealmConfiguration =
        RealmConfiguration.Builder()
            .name("wakewheel.realm")
            .build()
}
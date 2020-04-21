package com.example.wakewheel

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import androidx.fragment.app.Fragment
import com.example.wakewheel.di.AppModule
import com.example.wakewheel.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class App : Application(),
    HasBroadcastReceiverInjector,
    HasActivityInjector,
    HasServiceInjector,
    HasSupportFragmentInjector {

    @Inject
    lateinit var receiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
            .inject(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(getDefaultDbConfig())
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> =
        receiverInjector

    override fun activityInjector(): AndroidInjector<Activity> =
        activityInjector

    override fun serviceInjector(): AndroidInjector<Service> =
        serviceInjector

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        supportFragmentInjector

    private fun getDefaultDbConfig(): RealmConfiguration =
        RealmConfiguration.Builder()
            .name("wakewheel.realm")
            .build()
}
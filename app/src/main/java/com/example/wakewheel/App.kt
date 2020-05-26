package com.example.wakewheel

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import androidx.fragment.app.Fragment
import com.example.wakewheel.di.AppModule
import com.example.wakewheel.di.DaggerAppComponent
import com.example.wakewheel.heartrate.AutoConnectBleDevice
import com.example.wakewheel.heartrate.receivers.BluetoothGattReceiver
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Provider

@ExperimentalCoroutinesApi
class App : Application(),
    HasBroadcastReceiverInjector,
    HasActivityInjector,
    HasServiceInjector,
    HasSupportFragmentInjector {

    @Inject lateinit var receiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    @Inject lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject lateinit var autoConnectBleDeviceProvider: Provider<AutoConnectBleDevice>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
            .inject(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(getDefaultDbConfig())

        registerReceiver(BluetoothGattReceiver(), BluetoothGattReceiver.intentFilter)
        autoConnectBleDeviceProvider.get().invoke()
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
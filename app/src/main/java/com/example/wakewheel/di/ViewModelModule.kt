package com.example.wakewheel.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wakewheel.heartrate.view.ManageBleDeviceViewModel
import com.example.wakewheel.monitoring.MonitoringViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@ExperimentalCoroutinesApi
@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(ManageBleDeviceViewModel::class)
    internal abstract fun bindManageBleDeviceViewModel(viewModel: ManageBleDeviceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MonitoringViewModel::class)
    internal abstract fun bindMonitoringViewModel(viewModel: MonitoringViewModel): ViewModel
}
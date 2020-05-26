package com.example.wakewheel.di

import com.example.wakewheel.data.InMemoryAlarmReasonRepo
import com.example.wakewheel.data.specifications.RealmSpecificationsRepo
import com.example.wakewheel.data.specifications.SpecificationsMapper
import com.example.wakewheel.monitoring.AlarmReasonRepo
import com.example.wakewheel.monitoring.AlarmSpecificationChecker
import com.example.wakewheel.monitoring.SleepMonitor
import com.example.wakewheel.monitoring.SpecificationsRepo
import com.example.wakewheel.facerecognition.EyesMeasurementEventBus
import com.example.wakewheel.heartrate.HeartRateEventBus
import dagger.Module
import dagger.Provides
import io.realm.Realm
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
class MonitoringModule {

    @Singleton
    @Provides
    fun provideSleepMonitor(
        heartRateEventBus: HeartRateEventBus,
        eyesMeasurementEventBus: EyesMeasurementEventBus,
        alarmSpecificationChecker: AlarmSpecificationChecker
    ) =
        SleepMonitor(heartRateEventBus, eyesMeasurementEventBus, alarmSpecificationChecker)

    @Singleton
    @Provides
    fun provideEyesMeasurementEventBus() =
        EyesMeasurementEventBus()

    @Provides
    fun provideAlarmSpecifications() =
        AlarmSpecificationChecker()

    @Provides
    fun provideSpecificationsRepo(mapper: SpecificationsMapper, realm: Realm): SpecificationsRepo =
        RealmSpecificationsRepo(mapper, realm)

    @Provides
    fun provideSpecificationsMapper() =
        SpecificationsMapper()

    @Singleton
    @Provides
    fun provideAlarmReasonRepo(): AlarmReasonRepo =
        InMemoryAlarmReasonRepo()
}
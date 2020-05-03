package com.example.wakewheel.data.specifications

import com.example.wakewheel.monitoring.EyesDataSpecification
import com.example.wakewheel.monitoring.HeartRateSpecification
import com.example.wakewheel.monitoring.SpecificationsRepo
import io.realm.Realm

class RealmSpecificationsRepo(
    private val mapper: SpecificationsMapper,
    private val realm: Realm
) : SpecificationsRepo {

    override fun insertOrUpdate(data: HeartRateSpecification) {
        realm.executeTransaction {
            it.insertOrUpdate(mapper.map(data))
        }
    }

    override fun insertOrUpdate(data: EyesDataSpecification) {
        realm.executeTransaction {
            it.insertOrUpdate(mapper.map(data))
        }
    }

    override fun fetchHeartRateSpecification(): HeartRateSpecification? =
        realm.where(RealmHeartRateSpecification::class.java)
            .findFirst()
            ?.let { mapper.map(realm.copyFromRealm(it)) }

    override fun fetchEyesDataSpecification(): EyesDataSpecification? =
        realm.where(RealmEyesDataSpecification::class.java)
            .findFirst()
            ?.let { mapper.map(realm.copyFromRealm(it)) }
}
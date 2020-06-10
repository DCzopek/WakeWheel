package com.example.wakewheel.data.specifications

import com.example.wakewheel.data.RealmApi
import com.example.wakewheel.monitoring.EyesDataSpecification
import com.example.wakewheel.monitoring.HeartRateSpecification
import com.example.wakewheel.monitoring.SpecificationsRepo

class RealmSpecificationsRepo(
    private val mapper: SpecificationsMapper,
    private val realmApi: RealmApi
) : SpecificationsRepo {

    override fun insertOrUpdate(data: HeartRateSpecification) {
        realmApi.getInstance()
            .use { realm ->
                realm.executeTransaction {
                    it.insertOrUpdate(mapper.map(data))
                }

            }

    }

    override fun insertOrUpdate(data: EyesDataSpecification) {
        realmApi.getInstance()
            .use { realm ->
                realm.executeTransaction {
                    it.insertOrUpdate(mapper.map(data))
                }
            }
    }

    override fun fetchHeartRateSpecification(): HeartRateSpecification? =
        realmApi.getInstance()
            .use { realm ->
                realm.where(RealmHeartRateSpecification::class.java)
                    .findFirst()
                    ?.let { mapper.map(realm.copyFromRealm(it)) }
            }

    override fun fetchEyesDataSpecification(): EyesDataSpecification? =
        realmApi.getInstance()
            .use { realm ->
                realm.where(RealmEyesDataSpecification::class.java)
                    .findFirst()
                    ?.let { mapper.map(realm.copyFromRealm(it)) }
            }
}
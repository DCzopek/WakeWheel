package com.example.wakewheel.data.specifications

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmEyesDataSpecification(
    @PrimaryKey var pk: Int? = 1,
    var timeThreshold: Long? = null,
    var valueThreshold: Float? = null
) : RealmObject()
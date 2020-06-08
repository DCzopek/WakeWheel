package com.example.wakewheel.data

import io.realm.Realm

class RealmApi {
    fun getInstance(): Realm =
        Realm.getDefaultInstance()
}
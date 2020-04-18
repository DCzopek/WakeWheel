package com.example.wakewheel.dbtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wakewheel.R
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_dbtest.insert

class DbTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dbtest)

        insert.setOnClickListener {
            insertRecord()
        }
    }

    private fun insertRecord() {
        Realm.getDefaultInstance()
            .use { realm ->
                realm.executeTransaction {
                    RealmHeartRateData(heartRate = 50)
                        .let { heartData ->
                            println("Inserting heart data with date: ${heartData.date}")
                            it.insertOrUpdate(heartData)
                        }
                }
            }
    }
}
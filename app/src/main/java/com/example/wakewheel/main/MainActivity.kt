package com.example.wakewheel.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wakewheel.R
import dagger.android.AndroidInjection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import permissions.dispatcher.RuntimePermissions

@ExperimentalCoroutinesApi
@RuntimePermissions
class MainActivity : AppCompatActivity() {

    private var backPressListener: BackPressListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    fun attachBackPressListener(listener: BackPressListener) {
        backPressListener = listener
    }

    fun detachBackPressListener() {
        backPressListener = null
    }

    override fun onBackPressed() {
        backPressListener?.onBackPress()
            ?: super.onBackPressed()
    }
}

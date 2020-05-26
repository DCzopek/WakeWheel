package com.example.wakewheel.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wakewheel.Const
import com.example.wakewheel.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_splash, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        MainScope().launch {
            delay(2000)
            activity?.getPreferences(Context.MODE_PRIVATE)
                ?.let { prefs ->
                    with(findNavController()) {
                        when (prefs.getBoolean(Const.FIRST_USE_KEY, true)) {
                            false -> navigate(R.id.action_splashFragment_to_mainFragment)
                            else -> {
                                prefs.edit().putBoolean(Const.FIRST_USE_KEY, false).apply()
                                navigate(R.id.action_splashFragment_to_welcomeFragment)
                            }
                        }
                    }
                }
        }
    }
}
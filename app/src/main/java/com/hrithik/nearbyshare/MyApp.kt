package com.hrithik.nearbyshare

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    companion object {
        lateinit var instance: MyApp
        fun getContext(): Context = instance.applicationContext
    }
}
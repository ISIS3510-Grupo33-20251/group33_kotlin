package com.example.universe

import android.app.Application
import com.example.universe.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UniverseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createNotificationChannel(this)
    }
}
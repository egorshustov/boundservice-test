package com.egorshustov.boundservicetest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "BoundServiceTest Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            serviceChannel.setSound(null, null)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "BoundServiceTest Channel"
    }
}
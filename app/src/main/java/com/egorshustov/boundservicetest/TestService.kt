package com.egorshustov.boundservicetest

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.egorshustov.boundservicetest.App.Companion.NOTIFICATION_CHANNEL_ID


class TestService : Service() {
    private val testBinder = TestBinder()
    private var handler = Handler()
    var progress: Int = 0
    var maxValue: Int = 100000
        private set
    var isPaused: Boolean = true
        private set

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Сервис запущен")
            .setSmallIcon(R.drawable.ic_face)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return testBinder
    }

    private fun startPretendLongRunningTask() {
        val runnable = object : Runnable {
            override fun run() {
                if (progress >= maxValue || isPaused) {
                    Log.d(TAG, "run: removing callbacks")
                    handler.removeCallbacks(this)
                    pausePretendLongRunningTask()
                } else {
                    Log.d(TAG, "run: progress: $progress")
                    progress += 100
                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.postDelayed(runnable, 100)
    }

    fun pausePretendLongRunningTask() {
        isPaused = true
    }

    fun unPausePretendLongRunningTask() {
        isPaused = false
        startPretendLongRunningTask()
    }

    inner class TestBinder : Binder() {
        fun getTestService(): TestService {
            return this@TestService
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved")
        super.onTaskRemoved(rootIntent)
        //stopSelf()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return true
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val TAG = "TestService"
    }
}
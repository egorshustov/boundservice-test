package com.egorshustov.boundservicetest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var testService: TestService? = null
    private lateinit var mainViewModel: MainViewModel

    private var handler: Handler? = null
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        button_updates.setOnClickListener {
            toggleUpdates()
        }

        mainViewModel.getTestBinder().observe(this, Observer { testBinder ->
            if (testBinder != null) {
                Log.d(TAG, "Observer: connected to service")
                testService = testBinder.getTestService()
                testService?.let { testService ->
                    mainViewModel.setIsProgressUpdating(!testService.isPaused)
                }
            } else {
                Log.d(TAG, "Observer: disconnected from service")
                testService = null
            }
        })

        mainViewModel.getIsProgressUpdating().observe(this, Observer { isProgressUpdating ->
            handler = Handler()
            runnable = object : Runnable {
                override fun run() {
                    Log.d(TAG, "run")
                    if (isProgressUpdating) {
                        val testService = testService
                        if (mainViewModel.getTestBinder().value != null && testService != null) {
                            progress_bar.progress = testService.progress
                            progress_bar.max = testService.maxValue
                            val progress = (100 * testService.progress / testService.maxValue).toString() + "%"
                            text_view.text = progress

                            if (testService.progress == testService.maxValue) {
                                handler?.removeCallbacks(this)
                                mainViewModel.setIsProgressUpdating(false)
                            } else {
                                handler?.postDelayed(this, 100)
                            }
                        }
                    } else {
                        handler?.removeCallbacks(this)
                    }
                }
            }

            if (isProgressUpdating) {
                button_updates.text = "Pause"
                handler?.postDelayed(runnable, 100)
            } else {
                testService?.let {
                    if (it.progress == it.maxValue) {
                        button_updates.text = "Restart"
                    } else {
                        button_updates.text = "Start"
                    }
                }
            }
        })
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        startService()
    }

    private fun startService() {
        val serviceIntent = Intent(this, TestService::class.java)
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
        bindService()
    }

    private fun bindService() {
        val serviceIntent = Intent(this, TestService::class.java)
        bindService(serviceIntent, mainViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE)
    }

    private fun toggleUpdates() {
        testService?.let {
            if (it.progress == it.maxValue) {
                it.progress = 0
                button_updates.text = "Start"
            } else {
                if (it.isPaused) {
                    handler?.removeCallbacks(runnable)
                    it.unPausePretendLongRunningTask()
                    mainViewModel.setIsProgressUpdating(true)
                } else {
                    handler?.postDelayed(runnable, 100)
                    it.pausePretendLongRunningTask()
                    mainViewModel.setIsProgressUpdating(false)
                }
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (mainViewModel.getTestBinder().value != null) {
            unbindService(mainViewModel.getServiceConnection())
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        handler?.removeCallbacks(runnable)
        super.onDestroy()
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}

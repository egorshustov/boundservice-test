package com.egorshustov.boundservicetest

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val isProgressUpdating = MutableLiveData<Boolean>()
    private val testBinder = MutableLiveData<TestService.TestBinder>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "ServiceConnection: disconnected from service")
            testBinder.postValue(null)
        }

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            Log.d(TAG, "ServiceConnection: connected to service")
            val binder = iBinder as TestService.TestBinder
            testBinder.postValue(binder)
        }
    }

    fun getIsProgressUpdating(): LiveData<Boolean> {
        return isProgressUpdating
    }

    fun setIsProgressUpdating(isUpdating: Boolean) {
        isProgressUpdating.postValue(isUpdating)
    }

    fun getTestBinder(): LiveData<TestService.TestBinder> {
        return testBinder
    }

    fun getServiceConnection(): ServiceConnection {
        return serviceConnection
    }

    private companion object {
        const val TAG = "MainViewModel"
    }
}
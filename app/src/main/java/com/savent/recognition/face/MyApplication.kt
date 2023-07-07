package com.savent.recognition.face

import android.app.Application

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _networkStatus = MutableSharedFlow<ConnectivityObserver.Status>()
    val networkStatus = _networkStatus.asSharedFlow()
    var currentNetworkStatus: ConnectivityObserver.Status? = null

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@MyApplication)
            modules(appModule)
        }
        applicationScope.launch {
            performanceNetworkTask()
        }


    }

    private suspend fun performanceNetworkTask() {
        val connectivityObserver = NetworkConnectivityObserver(applicationContext)
        connectivityObserver.observe().collectLatest {
            currentNetworkStatus = it
            _networkStatus.emit(it)
            if (it == ConnectivityObserver.Status.Available) {

            }
        }
    }

}
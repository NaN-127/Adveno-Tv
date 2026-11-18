package com.adveno.tv.app

import android.app.Application
import com.adveno.tv.core.di.appModule
import com.adveno.tv.core.network.DeviceCredentialStore
import com.adveno.tv.core.telemetry.HeartbeatMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AdvenoApp : Application() {

    private val heartbeatMonitor: HeartbeatMonitor by inject()
    private val credentialStore: DeviceCredentialStore by inject()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AdvenoApp)
            modules(appModule)
        }

        applicationScope.launch { credentialStore.warmUp() }

        // Start background telemetry heartbeat
        heartbeatMonitor.start()
    }
}


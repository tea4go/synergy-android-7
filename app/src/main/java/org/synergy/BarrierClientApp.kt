package org.synergy

import android.app.Application
import org.synergy.base.utils.Log
import timber.log.Timber

@Suppress("unused")
class BarrierClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Log.logLevel = Log.Level.DEBUG
        } else {
            Log.logLevel = Log.Level.ERROR
        }
    }
}
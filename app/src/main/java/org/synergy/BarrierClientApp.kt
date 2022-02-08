package org.synergy

import android.app.Application
import org.synergy.base.utils.BarrierDebugTree
import org.synergy.base.utils.Log
import org.synergy.base.utils.Timber

@Suppress("unused")
class BarrierClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(BarrierDebugTree())
            Log.logLevel = Log.Level.DEBUG
        } else {
            Log.logLevel = Log.Level.ERROR
        }
    }
}
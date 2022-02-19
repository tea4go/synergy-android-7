package org.synergy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.synergy.utils.BarrierDebugTree
import org.synergy.utils.Timber

@HiltAndroidApp
class BarrierClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(BarrierDebugTree())
        }
    }
}
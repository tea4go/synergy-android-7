package org.synergy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.synergy.barrier.base.utils.BarrierDebugTree
import org.synergy.barrier.base.utils.Timber

@HiltAndroidApp
class BarrierClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(BarrierDebugTree())
        }
    }
}
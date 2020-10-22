package com.jack.nars.waver

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@Suppress("unused")
@HiltAndroidApp
class WaverApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        delayedCreate()
        Timber.plant(Timber.DebugTree())
    }

    fun delayedCreate() {}
}

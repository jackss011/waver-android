package com.jack.nars.waver

import android.app.Application
import timber.log.Timber


class WaverApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}

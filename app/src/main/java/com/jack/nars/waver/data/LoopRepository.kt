package com.jack.nars.waver.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LoopRepository @Inject constructor(@ApplicationContext appContext: Context) {
    init {
        Timber.i("Initialized repository")
    }

    val staticLoops = LoopLoader.getAllLoops(appContext)
}
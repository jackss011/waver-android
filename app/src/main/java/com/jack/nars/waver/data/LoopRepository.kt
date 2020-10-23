package com.jack.nars.waver.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LoopRepository @Inject constructor(@ApplicationContext appContext: Context) {
    init {
        Timber.i("Initialized repository")
    }

    val staticLoops = StaticLoopsInflater.inflate(appContext)

    private val _activeComposition = MutableLiveData<CompositionData?>(null)
    val activeCompositionData: LiveData<CompositionData?> get() = _activeComposition

    fun updateActiveComposition(composition: CompositionData) {
        _activeComposition.value = composition
    }

    fun resetActiveComposition() {
        _activeComposition.value = null
    }
}
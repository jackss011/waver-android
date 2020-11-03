package com.jack.nars.waver.data

import android.content.Context
import android.os.storage.StorageVolume
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LoopRepository @Inject constructor(@ApplicationContext appContext: Context) {

    val staticLoops = StaticLoopsInflater.inflate(appContext)

    private val _activeComposition = MutableLiveData<CompositionData?>(null)
    val activeCompositionData: LiveData<CompositionData?> get() = _activeComposition

    fun updateActiveComposition(composition: CompositionData) {
        _activeComposition.value = composition
    }

    fun resetActiveComposition() {
        _activeComposition.value = null
    }


//    private val _isPlaying = MutableLiveData(false)
//    val isPlaying: LiveData<Boolean> = _isPlaying
//
//
//    fun notifyPlaying(isPlaying: Boolean) {
//        _isPlaying.value = isPlaying
//    }
}
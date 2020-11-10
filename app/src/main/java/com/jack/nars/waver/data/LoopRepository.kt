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

    private val _activeComposition = MutableLiveData(CompositionData())
    val activeCompositionData: LiveData<CompositionData> get() = _activeComposition

    fun updateActiveComposition(composition: CompositionData) {
        _activeComposition.value = composition
    }

    fun resetActiveComposition() {
        _activeComposition.value = CompositionData()
    }

    fun activateLoop(id: String) {
        val old = _activeComposition.value!!
        val new = old.copy(loops = old.loops + CompositionItem(id))
        updateActiveComposition(new)
    }

    fun deactivateLoop(id: String) {
        val old = _activeComposition.value!!
        val new = old.copy(loops = old.loops.filter { it.id != id })
        updateActiveComposition(new)
    }

    fun setLoopIntensity(id: String, value: Float) {
        val old = _activeComposition.value!!
        val new =
            old.copy(loops = old.loops.map { if (it.id == id) it.copy(volume = value) else it })
        updateActiveComposition(new)
    }


//    private val _isPlaying = MutableLiveData(false)
//    val isPlaying: LiveData<Boolean> = _isPlaying
//
//
//    fun notifyPlaying(isPlaying: Boolean) {
//        _isPlaying.value = isPlaying
//    }
}
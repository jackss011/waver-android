 package com.jack.nars.waver.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LoopRepository @Inject constructor(@ApplicationContext appContext: Context) {

    val staticLoops = StaticLoopsInflater.inflate(appContext)


    // ========== ACTIVE COMPOSITION ===========
    private val _activeComposition = MutableLiveData(CompositionData())
    val activeCompositionData: LiveData<CompositionData> get() = _activeComposition

    private val compositionCache = CompositionCache()


    fun updateActiveComposition(composition: CompositionData) {
        _activeComposition.value = composition
    }


    fun resetActiveComposition() {
        _activeComposition.value = CompositionData()
    }


    fun activateLoop(id: String) {
        val old = _activeComposition.value!!

        if (old.loops.any { it.id == id }) return

        val cached = compositionCache.retrieveLoop(id)
        val toAdd = if (cached != null) CompositionItem(id, cached.volume) else CompositionItem(id)
        val new = old.copy(loops = old.loops + toAdd)

        updateActiveComposition(new)
    }


    fun deactivateLoop(id: String) {
        val old = _activeComposition.value!!

        val toRemoveList = old.loops.filter { it.id == id }.apply {
            firstOrNull()?.let { compositionCache.saveLoop(it) }
        }

        val new = old.copy(loops = old.loops - toRemoveList)
        updateActiveComposition(new)
    }


    fun setLoopIntensity(id: String, value: Float) {
        val old = _activeComposition.value!!
        val new =
            old.copy(loops = old.loops.map { if (it.id == id) it.copy(volume = value) else it })
        updateActiveComposition(new)
    }


    // ========== PREVIEW ===========
    private val intensityPreviewMap = MutableLiveData<Map<String, Float>>(emptyMap())


    fun setLoopIntensityPreview(id: String, value: Float) {
        val old = intensityPreviewMap.value!!
        intensityPreviewMap.value = old + (id to value)
    }


    fun stopLoopIntensityPreview(id: String) {
        val old = intensityPreviewMap.value!!
        intensityPreviewMap.value = old - id
    }


    fun stopAllLoopIntensityPreview() {
        intensityPreviewMap.value = emptyMap()
    }


    // ========= PLAYABLE COMPOSITION =============
    val playableComposition = MediatorLiveData<CompositionData>()

    init {
        playableComposition.addSource(_activeComposition) {
            playableComposition.value =
                calculatePlayableComposition(it, intensityPreviewMap.value!!)
        }

        playableComposition.addSource(intensityPreviewMap) {
//            Timber.v("Update intensity preview map: $it")

            playableComposition.value =
                calculatePlayableComposition(activeCompositionData.value!!, it)

        }
    }

    private fun calculatePlayableComposition(
        composition: CompositionData,
        previewMap: Map<String, Float>,
    ): CompositionData {
        val newLoops = composition.loops.map {
            val previewIntensity = previewMap[it.id]
            if (previewIntensity != null)
                it.copy(volume = previewIntensity)
            else
                it
        }

        return composition.copy(loops = newLoops)
    }
}
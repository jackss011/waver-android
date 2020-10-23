package com.jack.nars.waver.list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jack.nars.waver.data.Loop
import com.jack.nars.waver.data.LoopRepository
import timber.log.Timber
import kotlin.math.abs


data class DisplayLoop(val loop: Loop, var intensity: Float, var enabled: Boolean) {
}

class LoopListModel @ViewModelInject
constructor(private val loopRepository: LoopRepository) : ViewModel() {

    private val staticLoops = loopRepository.staticLoops.toList()

    private val _displayedLoopsMap = MutableLiveData(
        staticLoops.map { it.id to DisplayLoop(it, 0.5f, false) }.toMap()
    )
    val displayLoops: LiveData<List<DisplayLoop>>
        get() =
            Transformations.map(_displayedLoopsMap) { it.values.toList() }

    private fun setLoopEnabled(id: String, enabled: Boolean) {
        val m = mutableMapOf<String, DisplayLoop>()
        val map = _displayedLoopsMap.value ?: throw IllegalStateException("null map")

        for (k in map.keys) {
            val item = map[k]?.copy() ?: throw IllegalStateException("null item")
            if (item.loop.id == id) item.enabled = enabled
            m[k] = item
        }

        _displayedLoopsMap.value = m

//        val map = _displayedLoopsMap.value?.toMutableMap() ?: return
//        map[id] = map[id]?.copy(enabled = enabled) ?: return
//        _displayedLoopsMap.value = map

        Timber.i("FIRE - Loop enabled $enabled")
    }

    private fun changeLoopIntensity(id: String, intensity: Float) {
        val m = mutableMapOf<String, DisplayLoop>()
        val map = _displayedLoopsMap.value ?: throw IllegalStateException("null map")

        for (k in map.keys) {
            val item = map[k]?.copy() ?: throw IllegalStateException("null item")
            if (item.loop.id == id) item.intensity = intensity
            m[k] = item
        }
        Timber.w("INT - Pre change = $intensity")

        _displayedLoopsMap.value = m
    }

    fun onLoopUpdated(id: String, enabled: Boolean, intensity: Float) {
        val displayLoop = _displayedLoopsMap.value?.get(id)
            ?: throw IllegalStateException("display loop not found")

        if (enabled != displayLoop.enabled) {
            Timber.i("FIRE - Calling loop enabled\n current: ${displayLoop.enabled}\n next: $enabled")
            setLoopEnabled(id, enabled)
        }

        if (abs(intensity - displayLoop.intensity) > 0.02) {
            changeLoopIntensity(id, intensity)
        }
    }

    private fun getCompositionData() {

    }


}
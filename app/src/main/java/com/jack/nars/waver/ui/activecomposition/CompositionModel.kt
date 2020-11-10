package com.jack.nars.waver.ui.activecomposition

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.LoopRepository


data class LoopDisplayInfo(val id: String, val title: String, val intensity: Float = .5f)


class CompositionModel @ViewModelInject
constructor(private val loopRepository: LoopRepository) : ViewModel() {
    val activeLoops = loopRepository.activeCompositionData.map { c ->
        c.loops.mapNotNull { item ->
            val loop =
                loopRepository.staticLoops.find { item.id == it.id } ?: return@mapNotNull null
            LoopDisplayInfo(loop.id, loop.title, item.volume)
        }
    }

    fun onChangeLoopIntensity(id: String, intensity: Float) {
        loopRepository.setLoopIntensity(id, intensity)
        loopRepository.stopLoopIntensityPreview(id)
    }

    fun onPreviewLoopIntensity(id: String, intensity: Float) {
        loopRepository.setLoopIntensityPreview(id, intensity)
    }
}

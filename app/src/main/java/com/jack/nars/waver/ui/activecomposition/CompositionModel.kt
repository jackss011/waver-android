package com.jack.nars.waver.ui.activecomposition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.repos.LoopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class LoopDisplayInfo(val id: String, val title: String, val intensity: Float = .5f)

@HiltViewModel
class CompositionModel @Inject
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

    fun stopAllPreviews() {
        loopRepository.stopAllLoopIntensityPreview()
    }

    fun onRemoveLoop(id: String) {
        loopRepository.deactivateLoop(id)
    }
}

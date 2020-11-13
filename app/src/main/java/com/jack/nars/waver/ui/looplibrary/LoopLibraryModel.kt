package com.jack.nars.waver.ui.looplibrary

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.ControlsRepository
import com.jack.nars.waver.data.LoopRepository
import com.jack.nars.waver.data.PlaybackRequest


class LoopLibraryModel @ViewModelInject constructor(
    private val loopRepository: LoopRepository,
    private val controlsRepository: ControlsRepository
) : ViewModel() {

    val staticLoops = loopRepository.staticLoops
    val activeLoopIds = loopRepository.activeCompositionData
        .map { composition -> composition.loops.map { it.id } }


    fun onLoopClicked(id: String, checked: Boolean) {
        if (checked) {
            loopRepository.activateLoop(id)
            controlsRepository.sendPlaybackRequest(PlaybackRequest.PLAY)
        } else {
            loopRepository.deactivateLoop(id)
        }
    }
}
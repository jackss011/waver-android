package com.jack.nars.waver.ui.looplibrary

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.repos.ControlsRepository
import com.jack.nars.waver.data.repos.LoopRepository
import com.jack.nars.waver.data.repos.PlaybackRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoopLibraryModel @Inject constructor(
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
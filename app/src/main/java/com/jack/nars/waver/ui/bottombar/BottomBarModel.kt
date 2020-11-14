package com.jack.nars.waver.ui.bottombar

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.repos.ControlsRepository
import com.jack.nars.waver.data.repos.LoopRepository


class BottomBarModel @ViewModelInject
constructor(
    private val loopRepository: LoopRepository,
    private val controlsRepository: ControlsRepository
) : ViewModel() {

    val hasPlayableComposition: LiveData<Boolean> =
        loopRepository.activeCompositionData.map { it.isPlayable }

    val masterVolume = controlsRepository.masterVolume

    fun onMasterVolumeConfirmed(v: Float) {
        controlsRepository.updateMasterVolume(v)
    }
}
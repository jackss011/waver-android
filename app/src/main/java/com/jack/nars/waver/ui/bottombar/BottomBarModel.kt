package com.jack.nars.waver.ui.bottombar

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.MainModel
import com.jack.nars.waver.data.ControlsRepository
import com.jack.nars.waver.data.LoopRepository
import timber.log.Timber


class BottomBarModel @ViewModelInject
constructor(
    private val loopRepository: LoopRepository,
    private val controlsRepository: ControlsRepository
) : ViewModel() {

    val hasPlayableComposition: LiveData<Boolean> =
        loopRepository.activeCompositionData.map { it != null && it.isPlayable }

    val masterVolume = controlsRepository.masterVolume
}
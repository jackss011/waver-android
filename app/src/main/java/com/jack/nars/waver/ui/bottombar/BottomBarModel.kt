package com.jack.nars.waver.ui.bottombar

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.repos.ControlsRepository
import com.jack.nars.waver.data.repos.LoopRepository
import com.jack.nars.waver.data.repos.PlaybackRequest
import com.jack.nars.waver.data.repos.PlaybackState


class BottomBarModel @ViewModelInject
constructor(
    private val loopRepo: LoopRepository,
    private val controlsRepo: ControlsRepository
) : ViewModel() {

    val hasPlayableComposition = loopRepo.activeCompositionData.map { it.isPlayable }

    val masterVolume = controlsRepo.masterVolume

    fun onMasterVolumeConfirmed(v: Float) {
        controlsRepo.updateMasterVolume(v)
    }

    fun onPlayPause() {
        controlsRepo.sendPlaybackRequest(PlaybackRequest.PLAY_PAUSE)
    }

    val isPlaying = controlsRepo.state.map { it == PlaybackState.PLAYING }
}
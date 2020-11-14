package com.jack.nars.waver

import android.media.session.PlaybackState
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.jack.nars.waver.data.repos.ControlsRepository


class MainModel @ViewModelInject
constructor(private val controlsRepository: ControlsRepository) : ViewModel() {

    fun onPbStateUpdate(playbackState: PlaybackState?) {
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        controlsRepository.notifyPlay(isPlaying)
    }

    val playbackRequest = controlsRepository.request

    fun donePlaybackRequest() {
        controlsRepository.donePlaybackRequest()
    }
}

//    val isPlaying: LiveData<Boolean> = controlsRepository.state.map {
//        it == com.jack.nars.waver.data.repos.PlaybackState.PLAYING
//    }

//    fun onPlayPause() {
//        controlsRepository.sendPlaybackRequest(PlaybackRequest.PLAY_PAUSE)
//    }
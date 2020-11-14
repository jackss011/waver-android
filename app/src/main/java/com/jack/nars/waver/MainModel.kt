package com.jack.nars.waver

import android.media.session.PlaybackState
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.repos.ControlsRepository
import com.jack.nars.waver.data.repos.PlaybackRequest


class MainModel @ViewModelInject
constructor(private val controlsRepository: ControlsRepository) : ViewModel() {

    fun onPlaybackUpdate(playbackState: PlaybackState?) {
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        controlsRepository.notifyPlay(isPlaying)
    }

    fun donePlaybackRequest() {
        controlsRepository.donePlaybackRequest()
    }

    val isPlaying: LiveData<Boolean> = controlsRepository.state.map {
        it == com.jack.nars.waver.data.repos.PlaybackState.PLAYING
    }

    val playbackRequest = controlsRepository.request

    fun onPlayPause() {
        controlsRepository.sendPlaybackRequest(PlaybackRequest.PLAY_PAUSE)
    }
}
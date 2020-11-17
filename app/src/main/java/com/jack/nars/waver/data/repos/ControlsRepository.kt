package com.jack.nars.waver.data.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jack.nars.waver.data.storage.LastStateStorage
import javax.inject.Inject
import javax.inject.Singleton


enum class PlaybackState {
    PLAYING,
    NOT_PLAYING,
}

enum class PlaybackRequest {
    PLAY,
    PAUSE,
    PLAY_PAUSE,
}


@Singleton
class ControlsRepository @Inject constructor(private val lastState: LastStateStorage) {

    val masterVolume: MutableLiveData<Float> = MutableLiveData(lastState.getMasterVolume())

    fun updateMasterVolume(volume: Float) {
        masterVolume.value = volume
        lastState.saveMasterVolume(volume)
    }

    private val _state = MutableLiveData(PlaybackState.NOT_PLAYING)
    val state: LiveData<PlaybackState> = _state

    fun notifyPlay(isPlaying: Boolean) {
        _state.value = if (isPlaying) PlaybackState.PLAYING else PlaybackState.NOT_PLAYING
    }

    private val _request: MutableLiveData<PlaybackRequest?> = MutableLiveData(null)
    val request: LiveData<PlaybackRequest?> = _request

    fun sendPlaybackRequest(r: PlaybackRequest) {
        _request.value = r
    }

    fun donePlaybackRequest() {
        _request.value = null
    }
}
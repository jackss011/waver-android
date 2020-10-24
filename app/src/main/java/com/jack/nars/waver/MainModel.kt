package com.jack.nars.waver

import android.media.session.MediaController
import android.media.session.PlaybackState
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jack.nars.waver.data.LoopRepository


class MainModel @ViewModelInject
constructor(private val loopRepository: LoopRepository) : ViewModel() {
    fun onPlaybackUpdate(playbackState: PlaybackState?) {
        _isPlaying.value = (playbackState?.state == PlaybackState.STATE_PLAYING)
    }

    var mediaController: MediaController? = null

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    fun onPlayPause() {
        val mc = mediaController ?: return

        if (mc.playbackState?.state == PlaybackState.STATE_PLAYING) {
            mc.transportControls.pause()
        } else {
            mc.transportControls.play()
        }
    }
}
package com.jack.nars.waver.sound.players

import android.content.Context
import android.media.MediaPlayer


abstract class Player(val context: Context) {
    interface SourceProvider {
        fun setAsDataSource(context: Context, mp: MediaPlayer)
    }

    protected var provider: SourceProvider? = null

    open fun prepare(provider: SourceProvider) {
        this.provider = provider
    }

    open fun buildMediaPlayer(): MediaPlayer {
        return MediaPlayer()
    }

    abstract fun play()
    abstract fun pause()
    abstract fun release()

    open var volume = 1f
        set(value) {
            if(value > 1f || value < 0f) throw IllegalArgumentException()

            field = value
        }

    open var isPlaying = false
        protected set
}
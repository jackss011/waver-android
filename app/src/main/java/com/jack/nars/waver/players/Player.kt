package com.jack.nars.waver.players

import android.content.Context
import android.media.MediaPlayer
import java.lang.IllegalArgumentException

abstract class Player {
    interface SourceProvider {
        fun setAsDataSource(context: Context, mp: MediaPlayer)
    }

    protected var provider: SourceProvider? = null

    fun prepare(provider: SourceProvider) {
        this.provider = provider
    }

    abstract fun play()
    abstract fun pause()
    abstract fun release()

    var volume = 1f
        set(value) {
            if(value > 1f || value < 0f) throw IllegalArgumentException()

            field = value
        }
}
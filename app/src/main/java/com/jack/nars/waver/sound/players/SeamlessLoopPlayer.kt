package com.jack.nars.waver.sound.players

import android.content.Context
import android.media.MediaPlayer
import android.util.Log


class SeamlessLoopPlayer(context: Context) : BasePlayer(context) {
    val tag = "SeamlessLoopPlayer"

    private lateinit var mpA: MediaPlayer
    private lateinit var mpB: MediaPlayer
    private val players by lazy { arrayOf(mpA, mpB) }
    private var activeMp: MediaPlayer? = null


    override fun prepare(provider: SourceProvider) {
        if(this.provider != null)
            throw IllegalStateException("Preparing player twice")

        super.prepare(provider)

        fun setupPlayer(): MediaPlayer {
            val mp = buildMediaPlayer()
            provider.setAsDataSource(context, mp)
            mp.prepare()
            return mp
        }

        mpA = setupPlayer()
        mpB = setupPlayer()

        volume = 1f

        mpA.setOnCompletionListener {
            completed(mpA, mpB)
            Log.d(tag, "A completed")
        }
        mpB.setOnCompletionListener {
            completed(mpB, mpA)
            Log.d(tag, "B completed")
        }

        mpA.setOnPreparedListener {
            prepared(mpA, mpB)
            Log.d(tag, "A prepared")
        }
        mpB.setOnPreparedListener {
            prepared(mpB, mpA)
            Log.d(tag, "B prepared")
        }

        mpA.setOnInfoListener { _, what, _ -> Log.d(tag, "info %d".format(what))
            false
         }

        activate(mpA, mpB)
        mpA.setNextMediaPlayer(mpB)
        mpB.setNextMediaPlayer(mpA)
    }


    private fun prepared(who: MediaPlayer, other: MediaPlayer) {
//        who.setNextMediaPlayer(other)
    }


    private fun activate(who: MediaPlayer, other: MediaPlayer) {
        activeMp = who
    }


    private fun completed(who: MediaPlayer, other: MediaPlayer) {
        activate(other, who)

        who.setNextMediaPlayer(null)
        who.stop()
        who.prepare()
        other.setNextMediaPlayer(who)
    }


    override fun play() {
        activeMp?.start() ?: throw IllegalStateException("Player is not prepared")
    }


    override fun pause() {
        activeMp?.pause()
    }


    override fun release() {
        players.forEach {
            it.stop()
            it.release()
        }
    }


    override var volume: Float
        get() = super.volume
        set(value) {
            super.volume = value

            mpA.setVolume(value, value)
            mpB.setVolume(value, value)
        }
}
package com.jack.nars.waver

import android.content.Context
import android.media.MediaPlayer

class AudioLoopPlayer {
    companion object {
        fun create(context: Context, resId: Int): AudioLoopPlayer {
            return AudioLoopPlayer().apply {
                prepare(context, resId)
            }
        }
    }


    private lateinit var context: Context
    private var resId: Int = 0
    private var volume = 1f

    private lateinit var mp1: MediaPlayer
    private lateinit var mp2: MediaPlayer
    private var activeMp: MediaPlayer? = null

//    var readyCount = 0

//    val audioAttributes = AudioAttributes.Builder()
//        .setUsage(AudioAttributes.USAGE_MEDIA)
//        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//        .build()


    private fun preparePlayer(context: Context, resId: Int): MediaPlayer {
        return MediaPlayer.create(context, resId).apply {
            setVolume(volume, volume)
        }
    }


    private fun mp1Done() {
        activeMp = mp2

//        Toast.makeText(context, "Done 1", Toast.LENGTH_SHORT).show()
        mp1.release()

        mp1 = preparePlayer(context, resId).apply {
            setOnPreparedListener {
//                Toast.makeText(context, "Prepared 1", Toast.LENGTH_SHORT).show()
                mp2.setNextMediaPlayer(it)
            }

            setOnCompletionListener {
                mp1Done()
            }
        }
    }

    private fun mp2Done() {
        activeMp = mp1

//        Toast.makeText(context, "Done 2", Toast.LENGTH_SHORT).show()
        mp2.release()

        mp2 = preparePlayer(context, resId).apply {
            setOnPreparedListener {
//                Toast.makeText(context, "Prepared 2", Toast.LENGTH_SHORT).show()
                mp1.setNextMediaPlayer(it)
            }

            setOnCompletionListener {
                mp2Done()
            }
        }
    }

    private fun prepare(context: Context, resId: Int) {
        this.context = context
        this.resId = resId

        mp1 = preparePlayer(context, resId)
        mp2 = preparePlayer(context, resId)

        mp1.setVolume(1f, 1f)
        mp2.setVolume(1f, 1f)

        activeMp = mp1

//        mp1.seekTo(40 * 1000)

        mp1.setNextMediaPlayer(mp2)

        mp1.setOnCompletionListener {
            mp1Done()
        }

        mp2.setOnCompletionListener {
            mp2Done()
        }

    }

    fun start() {
        activeMp?.start()
    }

    fun pause() {
        activeMp?.pause()
    }

    fun isPlaying(): Boolean {
        return activeMp?.isPlaying ?: false
    }

    fun setVolume(v: Float) {
        volume = v

        mp1.setVolume(volume, volume)
        mp2.setVolume(volume, volume)
    }

    fun getVolume(): Float {
        return volume
    }
}
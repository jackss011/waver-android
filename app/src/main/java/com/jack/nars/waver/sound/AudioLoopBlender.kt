package com.jack.nars.waver.sound

import android.content.Context
import android.media.MediaPlayer
import android.media.VolumeShaper
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class AudioLoopBlender {
    companion object {
        fun create(context: Context, resId: Int): AudioLoopBlender {
            return AudioLoopBlender().apply {
                prepare(context, resId)
            }
        }
    }

    private fun configRaise(): VolumeShaper.Configuration {
        return VolumeShaper.Configuration.Builder()
            .setCurve(floatArrayOf(0f, 0.1f, 0.3f, 0.5f, 1f),  floatArrayOf(0f, 0.316f, 0.546f, 0.7f, 1f))
            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
            .setDuration(blendDuration.toLong())
            .build()
    }

    private fun configFall(): VolumeShaper.Configuration {
        return VolumeShaper.Configuration.Builder(configRaise())
            .reflectTimes()
            .build()
    }


    private lateinit var context: Context
    private var resId: Int = 0

    private lateinit var mp1: MediaPlayer
    private var shaper1: VolumeShaper? = null
    private var shaper2: VolumeShaper? = null
    private lateinit var mp2: MediaPlayer
    private var activePlayerIndex = 0


    private val preStart = 100
    private val blendDuration = 500
    private val blendMargin = 1000

//    val audioAttributes = AudioAttributes.Builder()
//        .setUsage(AudioAttributes.USAGE_MEDIA)
//        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//        .build()


    private fun preparePlayer(context: Context, resId: Int): MediaPlayer {
        return MediaPlayer.create(context, resId)
    }


    private fun prepare(context: Context, resId: Int) {
        this.context = context
        this.resId = resId

        mp1 = preparePlayer(context, resId)
        mp2 = preparePlayer(context, resId)

        shaper1 = mp1.createVolumeShaper(configFall())
        shaper2 = mp2.createVolumeShaper(configRaise())
    }


    fun start() {
        mp1.start()

        val fadeAfter = mp1.duration - blendMargin

        Handler(Looper.getMainLooper()).postDelayed({
            Toast.makeText(context, "Fade", Toast.LENGTH_SHORT).show()
            mp2.start()
        }, fadeAfter.toLong() - preStart)

        Handler(Looper.getMainLooper()).postDelayed({
            shaper2?.apply(VolumeShaper.Operation.PLAY)
            shaper1?.apply(VolumeShaper.Operation.PLAY)


        }, fadeAfter.toLong())
    }


    fun pause() {

    }

    fun setVolume(v: Float) {

    }

    fun getVolume(): Float {
        return 1f
    }

    fun isPlaying(): Boolean {
        return false
    }
}




////         val configRaise = VolumeShaper.Configuration.Builder()
////            .setCurve(floatArrayOf(0f, 1f), floatArrayOf(0f, 1f))
////            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
////            .setDuration(duration)
////            .build()
//
////        val configRaise = VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.CUBIC_RAMP)
////            .invertVolumes()
////            .reflectTimes()
////            .setDuration(duration)
////            .build()
//
//val configRaise = VolumeShaper.Configuration.Builder()
//    .setCurve(floatArrayOf(0f, 0.1f, 0.3f, 0.5f, 1f),  floatArrayOf(0f, 0.316f, 0.546f, 0.7f, 1f))
////            .setCurve(floatArrayOf(0f, (0.25f * 1.5f), 1f),  floatArrayOf(0f, 0.675f, 1f))
////            .setCurve(floatArrayOf(0f, (0.25f * 1.5f), 1f),  floatArrayOf(0f, 0.5f, 1f))
//    .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
//    .setDuration(blendDuration.toLong())
//    .build()
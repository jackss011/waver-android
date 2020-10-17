package com.jack.nars.waver.old

import android.content.Context
import android.media.MediaPlayer
import android.media.VolumeShaper
import android.os.Handler
import android.os.Looper
import android.util.Log


const val TAG = "AudioLoopBlender"


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

    private lateinit var playerA: MediaPlayer
    private lateinit var playerB: MediaPlayer

    private var shaperA: VolumeShaper? = null
    private var shaperB: VolumeShaper? = null

    private val blendDuration = 400
    private val blendMargin = 2000
    private val monitorUpdateRate = 300

    private lateinit var monitor: Handler
    private val monitorRunnable = object: Runnable {
        override fun run() {
            monitorUpdate()
            monitor.postDelayed(this, monitorUpdateRate.toLong())
        }
    }

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

        monitor = Handler(Looper.getMainLooper())

        playerA = preparePlayer(context, resId)
        playerB = preparePlayer(context, resId)

        playerA.apply {
            setOnCompletionListener {
                it.stop()
                it.prepareAsync()

                Log.d(TAG, "Reset A")
            }

            setOnPreparedListener {
                Log.d(TAG, "Prepared A")
            }
        }

        playerB.apply {
            setOnCompletionListener {
                it.stop()
                it.prepareAsync()

                Log.d(TAG, "Reset B")
            }

            setOnPreparedListener {
                Log.d(TAG, "Prepared B")
            }
        }
    }

    private fun startMonitor() {
        monitor.post(monitorRunnable)
    }

    private fun stopMonitor() {
        shaperStartRequired = false
        monitor.removeCallbacks(monitorRunnable)
    }

    private fun isMonitorRunning(): Boolean {
        TODO("implement")
    }

    private fun setupShapers(A2B: Boolean) {
        shaperA?.close()
        shaperB?.close()

        val aConfig = if (A2B) configFall() else configRaise()
        val bConfig = if (A2B) configRaise() else configFall()

        shaperA = playerA.createVolumeShaper(aConfig)
        shaperB = playerB.createVolumeShaper(bConfig)
    }

    private var shaperStartRequired = false
    private var activeA = true

    private fun monitorUpdate() {
        Log.v(TAG, "Monitor Update")

        if (shaperStartRequired) {
            shaperStartRequired = false

            Log.d(TAG, "Starting shapers...")

            shaperA?.apply(VolumeShaper.Operation.PLAY)
            shaperB?.apply(VolumeShaper.Operation.PLAY)

        }

        if (playerA.isPlaying) {
            if (!playerB.isPlaying && playerA.currentPosition > (playerA.duration - blendMargin)) {
                Log.d(TAG, "Fade to B")

                setupShapers(true)
                playerB.start()

                shaperStartRequired = true
                activeA = false
            }
        }

        if (playerB.isPlaying) {
            if (!playerA.isPlaying && playerB.currentPosition > playerB.duration - blendMargin) {
                Log.d(TAG, "Fade to A")

                setupShapers(false)
                playerA.start()

                shaperStartRequired = true
                activeA = true
            }
        }
    }


    fun start() {
        if(activeA)
            playerA.start()
        else
            playerB.start()

        startMonitor()
    }



    fun pause() {
        stopMonitor()

        if(playerA.isPlaying) {
            playerA.pause()
        }

        if(playerB.isPlaying)
            playerB.pause()
    }

    fun release() {

    }

    private var volume: Float = 1f

    fun setVolume(v: Float) {
        volume = v

        playerA.setVolume(volume, volume)
        playerB.setVolume(volume, volume)
    }

    fun getVolume(): Float {
        return volume
    }

    fun isPlaying(): Boolean {
        return playerA.isPlaying || playerB.isPlaying
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



//private fun resetDonePlayers() {
//    if((playerA.currentPosition == playerA.duration) && !playerA.isPlaying) {
//        playerA.stop()
//        playerA.prepareAsync()
//
//        Log.d(TAG, "Reset A")
//    }
//
//    if((playerB.currentPosition == playerB.duration) && !playerB.isPlaying) {
//        playerB.stop()
//        playerB.prepareAsync()
//        Log.d(TAG, "Reset B")
//    }
//}
package com.jack.nars.waver.sound.players

import android.content.Context
import android.media.MediaPlayer
import android.media.VolumeShaper
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jack.nars.waver.old.TAG


class CrossfadeLoopPlayer(context: Context) : Player(context) {

    private val blendDuration = 400
    private val blendMargin = 2000
    private val monitorUpdateRate = 300


    private lateinit var playerA: MediaPlayer
    private lateinit var playerB: MediaPlayer
    private var activeA = true


    override fun prepare(provider: SourceProvider) {
        super.prepare(provider)

        monitor = Handler(Looper.getMainLooper())

        fun setupPlayer(): MediaPlayer {
            val mp = buildMediaPlayer()
            provider.setAsDataSource(context, mp)
            mp.prepare()
            return mp
        }

        playerA = setupPlayer()
        playerB = setupPlayer()

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


    override fun play() {
        if(activeA)
            playerA.start()
        else
            playerB.start()

        startMonitor()
    }


    override fun pause() {
        stopMonitor()

        if(playerA.isPlaying) {
            playerA.pause()
        }

        if(playerB.isPlaying)
            playerB.pause()
    }


    override fun release() {
        pause()

        playerA.release()
        playerB.release()
    }


    @Suppress("UNUSED_PARAMETER")
    override var isPlaying: Boolean
        get() = playerA.isPlaying || playerB.isPlaying
        set(value) {}


    override var volume: Float
        get() = super.volume
        set(value) {
            super.volume = value

            playerA.setVolume(volume, volume)
            playerB.setVolume(volume, volume)
        }


    // =================================================
    // ================ SHAPERS ========================
    // =================================================
    private var shaperA: VolumeShaper? = null
    private var shaperB: VolumeShaper? = null
    private var shaperStartRequired = false


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


    private fun setupShapers(A2B: Boolean) {
        shaperA?.close()
        shaperB?.close()

        val aConfig = if (A2B) configFall() else configRaise()
        val bConfig = if (A2B) configRaise() else configFall()

        shaperA = playerA.createVolumeShaper(aConfig)
        shaperB = playerB.createVolumeShaper(bConfig)
    }


    // =================================================
    // ================ MONITOR ========================
    // =================================================
    private lateinit var monitor: Handler
    private val monitorRunnable = object: Runnable {
        override fun run() {
            monitorUpdate()
            monitor.postDelayed(this, monitorUpdateRate.toLong())
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
}
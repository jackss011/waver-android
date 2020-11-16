package com.jack.nars.waver.players

import android.content.Context
import android.media.MediaPlayer
import android.media.VolumeShaper
import android.os.Handler
import android.os.Looper
import android.util.AndroidRuntimeException
import timber.log.Timber
import kotlin.math.roundToLong


class LoopPlayer(context: Context) : BasePlayer(context) {
    private val blendDuration = 400
    private val blendMargin = 2000
    private val monitorInterval = 300

    private val rampUpDuration = 200
    private val rampDownDuration = 200


    private val blendEnter = VolumeShaper.Configuration.Builder()
        .setCurve(
            floatArrayOf(0f, 0.1f, 0.3f, 0.5f, 1f), floatArrayOf(
                0f,
                0.316f,
                0.546f,
                0.7f,
                1f
            )
        )
        .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
        .setDuration(blendDuration.toLong())

    private val blendExit = VolumeShaper.Configuration.Builder(blendEnter.build())
        .reflectTimes()

    private val rampUp = VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.LINEAR_RAMP)
        .setDuration(rampUpDuration.toLong())

    private val rampDown = VolumeShaper.Configuration.Builder(rampUp.build())
        .reflectTimes()
        .setDuration(rampDownDuration.toLong())


    private var playerA: MediaPlayer = MediaPlayer()
    private var playerB: MediaPlayer = MediaPlayer()
    private var blendShaperA: VolumeShaper? = null
    private var blendShaperB: VolumeShaper? = null

    private var activePlayer = playerA
    private val inactivePlayer get() = if (activePlayer === playerA) playerB else playerA

    private val activeBlendShaper get() = if (activePlayer === playerA) blendShaperA else blendShaperB
    private val inactiveBlendShaper get() = if (activePlayer === playerA) blendShaperB else blendShaperA

    private val shouldTransition
        get() =
            activePlayer.run { currentPosition > (duration - blendMargin) }

    private val wantsPlay get() = volumeRamp.isRising


    private fun setupPlayer(mp: MediaPlayer, provider: SourceProvider) {
        provider.setAsDataSource(context, mp)
        mp.prepare()

        mp.setOnCompletionListener {
            it.stop()
            it.prepareAsync()

            Timber.i("Completed player")
        }
    }


    override fun prepare(provider: SourceProvider) {
        setupPlayer(playerA, provider)
        blendShaperA = playerA.createVolumeShaper(blendExit.build())
        setupPlayer(playerB, provider)
        blendShaperB = playerB.createVolumeShaper(blendEnter.build())

        volumeRamp.start()
    }

    private var shaperStart = false

    private val monitor = Monitor(monitorInterval) {
        if (shaperStart) {
            Timber.i("Start shapers")

            activeBlendShaper?.apply(VolumeShaper.Operation.PLAY)
            inactiveBlendShaper?.apply(VolumeShaper.Operation.PLAY)
            shaperStart = false
        }

        if (shouldTransition) {
            Timber.i("Transition")

            activeBlendShaper?.replace(blendExit.build(), VolumeShaper.Operation.REVERSE, false)
            inactiveBlendShaper?.replace(blendEnter.build(), VolumeShaper.Operation.REVERSE, false)

//            activeBlendShaper?.close()
//            inactiveBlendShaper?.close()
//            activeBlendShaper = activePlayer.createVolumeShaper(blendExit.build())
//            inactiveBlendShaper = inactivePlayer.createVolumeShaper(blendEnter.build())

            inactivePlayer.start()
            activePlayer = inactivePlayer
            shaperStart = true
        }
    }


    override fun play() {
        volumeRamp.raise()

        activePlayer.start()
        monitor.start()
    }


    override fun pause() {
        volumeRamp.fall()
    }


    private fun enteredPause() {
        if (playerA.isPlaying) {
            playerA.pause()
        }

        if (playerB.isPlaying)
            playerB.pause()

        shaperStart = false

        monitor.stop()
    }


    override fun release() {
        pause()

        monitor.stop()
        volumeRamp.stop()

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
            volumeRamp.target = value
        }


    private fun setPlayersVolume(v: Float) {
        playerA.setVolume(v, v)
        playerB.setVolume(v, v)
    }


    private val volumeRamp = AnimatedRamp(object : AnimatedRamp.Listener {
        override fun onRiseCompleted() {}
        override fun onFallCompleted() = enteredPause()
        override fun onUpdate(v: Float) = setPlayersVolume(v)
    })


    // ==================================
    // =========== HELPERS ==============
    // ==================================
    private open class Monitor(val interval: Int, val r: Runnable) {
        private val handler = Handler(
            Looper.myLooper()
                ?: throw AndroidRuntimeException("Animators may only be run on Looper threads")
        )

        private fun tick() {
            r.run()
            handler.postDelayed(posted, interval.toLong())
        }

        private val posted = { tick() }

        open fun start() = handler.post(posted)
        open fun stop() = handler.removeCallbacks(posted)
    }


    private class AnimatedRamp(val listener: Listener) {
        val frameTime = 1f / 60f
        var travelTime = 0.2f


        interface Listener {
            fun onRiseCompleted()
            fun onFallCompleted()
            fun onUpdate(v: Float)
        }


        var isRising = false
            private set

        private var animPosition = 0f


        fun raise() {
            isRising = true
        }

        fun fall() {
            isRising = false
        }


        var target = 1f
            set(value) {
                field = value
                notifyUpdate()
            }


        private fun update() {
            val needsUpdate = if (isRising) animPosition < 1f else animPosition > 0f

            if (needsUpdate) {
                var delta = frameTime / travelTime
                if (!isRising) delta *= -1
                animPosition += delta
                animPosition = animPosition.coerceIn(0f, 1f)

                if (animPosition == 1f)
                    listener.onRiseCompleted()
                else if (animPosition == 0f)
                    listener.onFallCompleted()

                notifyUpdate()
            }
        }

        private fun notifyUpdate() {
            listener.onUpdate(animPosition * target)
        }


        // ======== PARENT =========
        private var isStarted = false

        fun start() {
            if (isStarted) return

            isStarted = true
            handler.post(posted)
        }

        fun stop() {
            isStarted = false
            handler.removeCallbacks(posted)
        }

        private val handler = Handler(
            Looper.myLooper()
                ?: throw AndroidRuntimeException("Animators may only be run on Looper threads")
        )

        private val posted = { tick() }

        private fun tick() {
            update()
            handler.postDelayed(posted, (frameTime * 1000).roundToLong())
        }
    }
}

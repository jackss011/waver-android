package com.jack.nars.waver

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.media.browse.MediaBrowser
//import android.support.v4.media.MediaBrowserCompat
import android.media.session.MediaController
//import android.support.v4.media.session.MediaControllerCompat
import android.media.session.PlaybackState
//import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.jack.nars.waver.sound.AudioLoopBlender
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private lateinit var vibrator: Vibrator
    private lateinit var looper: AudioLoopBlender

    private lateinit var volumeBar: SeekBar
    private var logVolume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        volumeBar = findViewById(R.id.volume_bar)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        looper = AudioLoopBlender.create(this, R.raw.brown_noise)

        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                val p = progress.toFloat() / seek.max
                val v = if (logVolume) sliderToGain(p) else p
                looper.setVolume(v)
                Log.d("MY_APP", "position: %f, volume: %f".format(p, v))
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {}
        })


//        val duration = 300L
//
//        val configRaise = VolumeShaper.Configuration.Builder()
//            .setCurve(floatArrayOf(0f, 1f), floatArrayOf(1f, 0f))
//            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
//            .setDuration(duration)
//            .build()
//
//        val configFall = VolumeShaper.Configuration.Builder()
//            .setCurve(floatArrayOf(0f, 0f), floatArrayOf(1f, 1f))
//            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
//            .setDuration(duration)
//            .build()

        mediaBrowser = MediaBrowser(
            this,
            ComponentName(this, SoundService::class.java),
            connectionCallbacks,
            null // optional Bundle
        )
    }

    private lateinit var mediaBrowser: MediaBrowser

    private val connectionCallbacks = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            Log.d(TAG, "Media Service connected")

            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaController(this@MainActivity, token)
//                MediaController.setMediaController(this@MainActivity, mediaController)
            }

            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    private lateinit var playPause: Button

    private val controllerCallback = object: MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            Log.d(TAG, "State changed")
        }
    }

    fun buildTransportControls() {
//        val mediaController = MediaController.getMediaController()
        // Grab the view for the play/pause button
        playPause = findViewById<Button>(R.id.play_pause).apply {
            setOnClickListener {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly

                val pbState = mediaController.playbackState?.state
                if (pbState == PlaybackState.STATE_PLAYING) {
                    mediaController.transportControls.pause()
                } else {
                    mediaController.transportControls.play()
//                    mediaController.transportControls.pla
                }
            }
        }

        // Display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        if (pbState != null) {
            Log.d(TAG, "Initial state: %d".format(pbState.state))
        }
        else {
            Log.d(TAG, "Initial state: null")
        }

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()

//        val intent = Intent()
//            .setClass(this, SoundService::class.java)
//
//        bindService(intent)
    }

    override fun onStop() {
        super.onStop()

        mediaController.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }


    fun onClick(v: View) {
        val effect = when (v.id) {
            R.id.main_os -> VibrationEffect.createOneShot(2000, 255)
            R.id.main_t1 -> VibrationEffect.createWaveform(
                LongArray(60) { 16L },
                IntArray(60) { 200 },
                10)
            else -> null
        }

        if(effect != null) vibrator.vibrate(effect)
    }


    fun onClick2(v: View) {
        if(!looper.isPlaying())
            looper.start()
        else
            looper.pause()
    }

    fun onClick3(v: View) {
        logVolume = !logVolume
        Toast.makeText(this, "Log volume is" + if(logVolume) "true" else "false", Toast.LENGTH_SHORT).show()
    }
}


fun sliderToGain(position: Float, silent: Float = -30f, rollOff: Boolean = true): Float {
    var g = 10f.pow(((1 - position) * silent) / 20)

    // correction to get gain=0 for position=0
    if(rollOff)
        g -= 10f.pow(silent / 20) * (1 - position)

    return g
}
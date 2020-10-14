package com.jack.nars.waver

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button


class MainActivity : AppCompatActivity() {
    private lateinit var mediaBrowser: MediaBrowser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowser = MediaBrowser(
            this,
            ComponentName(this, SoundService::class.java),
            mediaBrowserCallbacks,
            null
        )
    }


    override fun onStart() {
        super.onStart()

        mediaBrowser.connect()
    }


    override fun onStop() {
        super.onStop()

        mediaController.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }


    private val mediaBrowserCallbacks = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            Log.d(TAG, "Media Service connected")

            mediaController = MediaController(this@MainActivity, mediaBrowser.sessionToken)
            buildTransportControls()
            mediaController.registerCallback(controllerCallback)
        }
    }


    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            Log.d(TAG, "State changed")

            updatePlaybackState()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Log.d(TAG, "Metadata changed")
        }
    }


    private lateinit var playPauseButton: Button


    private fun buildTransportControls() {
        playPauseButton = findViewById<Button>(R.id.play_pause).apply {
            setOnClickListener {
                when (mediaController.playbackState?.state) {
                    PlaybackState.STATE_PLAYING -> mediaController.transportControls.pause()
                    PlaybackState.STATE_PAUSED -> mediaController.transportControls.play()
                    PlaybackState.STATE_NONE -> mediaController.transportControls.play()
                    PlaybackState.STATE_STOPPED -> mediaController.transportControls.play()
                }
            }
        }

        updatePlaybackState()
    }


    private fun updatePlaybackState() {
        if(mediaController.playbackState?.state != PlaybackState.STATE_PLAYING)
            playPauseButton.text = "Play"
        else
            playPauseButton.text = "Pause"
    }
}
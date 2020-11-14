package com.jack.nars.waver

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.jack.nars.waver.databinding.ActivityMainBinding
import com.jack.nars.waver.service.SoundService
import com.jack.nars.waver.data.repos.PlaybackRequest
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainModel by viewModels()
    private lateinit var mediaBrowser: MediaBrowser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("MainActivity created")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        mediaBrowser = MediaBrowser(
            this,
            ComponentName(this, SoundService::class.java),
            mediaBrowserCallbacks,
            null
        )

        viewModel.playbackRequest.observe(this) {
            onPlaybackRequest(it)
        }

        Timber.d("ID: ${android.os.Process.myPid()}: ${android.os.Process.myTid()}")
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


    private fun onPlaybackRequest(r: PlaybackRequest?) {
        if (r != null) {
            val tc = mediaController.transportControls
            val state = mediaController.playbackState?.state

            when (r) {
                PlaybackRequest.PLAY -> tc.play()
                PlaybackRequest.PAUSE -> tc.pause()
                PlaybackRequest.PLAY_PAUSE -> {
                    if (state == PlaybackState.STATE_PLAYING) tc.pause() else tc.play()
                }
            }

            viewModel.donePlaybackRequest()
        }
    }


    private val mediaBrowserCallbacks = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            Timber.d("Media Service connected")

            mediaController = MediaController(this@MainActivity, mediaBrowser.sessionToken)
            mediaController.registerCallback(controllerCallback)

            onStateUpdate(mediaController.playbackState)
        }
    }


    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            Timber.i("Playback state changed")
            onStateUpdate(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Timber.i("Metadata changed")
        }
    }


    private fun onStateUpdate(state: PlaybackState?) {
        viewModel.onPlaybackUpdate(state)
    }
}
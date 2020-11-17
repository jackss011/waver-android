package com.jack.nars.waver

import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
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

    private val navHost by lazy {
        supportFragmentManager.findFragmentById(R.id.frag_nav_host) as NavHostFragment
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val navController by lazy { navHost.navController }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("MainActivity created")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        mediaBrowser = SoundService.createMediaBrowser(this, mediaBrowserCallbacks)
        viewModel.playbackRequest.observe(this) { onPlaybackRequest(it) }

        navController.addOnDestinationChangedListener(this.navigationChangeListener)

        Timber.d("ID: ${android.os.Process.myPid()}: ${android.os.Process.myTid()}")
    }


    override fun onStart() {
        super.onStart()

        mediaBrowser.connect()
    }


    override fun onStop() {
        super.onStop()

        mediaController.unregisterCallback(controllerCallbacks)
        mediaBrowser.disconnect()
    }


    // ========== NAVIGATION CONTROL ============
    //===========================================

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val navigationChangeListener =
        NavController.OnDestinationChangedListener { _, destination, arguments ->
            // TODO: implement
        }


    // ========== MEDIA CONTROL ==========
    // ===================================

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
            mediaController.registerCallback(controllerCallbacks)

            onStateUpdate(mediaController.playbackState)
        }
    }


    private val controllerCallbacks = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            Timber.i("Playback state changed")
            onStateUpdate(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Timber.i("Metadata changed")
        }
    }


    private fun onStateUpdate(state: PlaybackState?) {
        viewModel.onPbStateUpdate(state)
    }
}
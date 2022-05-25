package com.jack.nars.waver

import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.jack.nars.waver.data.repos.PlaybackRequest
import com.jack.nars.waver.databinding.ActivityMainBinding
import com.jack.nars.waver.service.SoundService
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

    private val drawerLayout get() = binding.drawerLayout

    @Suppress("MemberVisibilityCanBePrivate")
    val navController by lazy { navHost.navController }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("MainActivity created")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.run {
            lifecycleOwner = this@MainActivity

//            topAppBar.setNavigationOnClickListener {
//                drawerLayout.openDrawer(GravityCompat.START)
//            }

            topAppBar.setupWithNavController(navController, drawerLayout)
        }

        mediaBrowser = SoundService.createMediaBrowser(this, mediaBrowserCallbacks)
        viewModel.playbackRequest.observe(this) { onPlaybackRequest(it) }

        setupNavigation()

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
    private fun setupNavigation() {
        navController.addOnDestinationChangedListener(this.navigationChangeListener)

        findViewById<Button>(R.id.navBtnProfiles).setOnClickListener {
            navController.navigate(R.id.action_global_dest_profiles)
            drawerLayout.closeDrawers()
        }
    }


    private val navigationChangeListener =
        NavController.OnDestinationChangedListener { _, destination, arguments ->
//            when (destination) {
//
//            }
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
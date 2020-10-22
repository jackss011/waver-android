package com.jack.nars.waver

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.jack.nars.waver.databinding.ActivityMainBinding
import com.jack.nars.waver.service.COMMAND_MASTER_VOLUME
import com.jack.nars.waver.service.SoundService
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.CompositionItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import timber.log.Timber


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaBrowser: MediaBrowser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("MainActivity created")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaBrowser = MediaBrowser(
            this,
            ComponentName(this, SoundService::class.java),
            mediaBrowserCallbacks,
            null
        )

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


    private val mediaBrowserCallbacks = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            Timber.d("Media Service connected")

            mediaController = MediaController(this@MainActivity, mediaBrowser.sessionToken)
            buildTransportControls()
            mediaController.registerCallback(controllerCallback)
        }
    }


    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            Timber.i("Playback state changed")

            updatePlaybackState()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Timber.i("Metadata changed")
        }

        override fun onSessionEvent(event: String, extras: Bundle?) {
            Timber.i("Session Event: $event")
        }
    }


    private fun buildTransportControls() {
        binding.playPause.apply {
            fun play() {
                val testComposition = CompositionData(loops = listOf(
                    CompositionItem("test:brown_noise", 0.5f),
                    CompositionItem("test:ambient_music", volume = 0.3f)
                ))

                mediaController.transportControls.playFromMediaId(Json.encodeToString(testComposition), null)
            }

            setOnClickListener {
                when (mediaController.playbackState?.state) {
                    PlaybackState.STATE_PLAYING -> mediaController.transportControls.pause()
                    PlaybackState.STATE_PAUSED -> play()
                    PlaybackState.STATE_NONE -> play()
                    PlaybackState.STATE_STOPPED -> Timber.w("Can't play from stopped state")
                    else -> {}
                }
            }
        }

        binding.volumeBar.apply {
            setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    bar?.also {
                        val v = progress.toFloat() / it.max
                        mediaController.sendCommand(
                            COMMAND_MASTER_VOLUME,
                            Bundle(1).apply { putFloat(null, v) },
                            null
                        )
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }

        updatePlaybackState()
    }


    private fun updatePlaybackState() {
        if(mediaController.playbackState?.state != PlaybackState.STATE_PLAYING)
            binding.playPause.text = "Play"
        else
            binding.playPause.text = "Pause"
    }
}
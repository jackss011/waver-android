package com.jack.nars.waver

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import com.jack.nars.waver.sound.CompositionData
import com.jack.nars.waver.sound.CompositionItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.xmlpull.v1.XmlPullParser


const val tag: String = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var mediaBrowser: MediaBrowser
    private lateinit var volumeSeek: SeekBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowser = MediaBrowser(
            this,
            ComponentName(this, SoundService::class.java),
            mediaBrowserCallbacks,
            null
        )

        val parser = resources.getXml(R.xml.loops)

        while(true) {
            val n = parser.next()
            if(n == XmlPullParser.END_DOCUMENT) break

            if(n == XmlPullParser.START_TAG) {
                if (parser.attributeCount > 0) {
                    val id = parser.getAttributeResourceValue(0, R.string.title_home)
                    if(id > 0)
                        Log.d(tag, parser.getAttributeName(0) + ": " + resources.getResourceName(id))
                }
            }

        }
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
                    PlaybackState.STATE_STOPPED -> Log.w(TAG, "Can't play from stopped state")
                }
            }
        }

        volumeSeek = findViewById<SeekBar>(R.id.volume_bar).apply {
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
            playPauseButton.text = "Play"
        else
            playPauseButton.text = "Pause"
    }
}
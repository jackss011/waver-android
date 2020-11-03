package com.jack.nars.waver.service

import android.app.NotificationManager
import android.os.Bundle

import android.service.media.MediaBrowserService

//import androidx.media.MediaBrowserServiceCompat
import android.media.browse.MediaBrowser
//import android.support.v4.media.MediaBrowserCompat
import android.media.session.MediaSession
//import android.support.v4.media.session.MediaSessionCompat
import android.media.session.PlaybackState
import android.app.PendingIntent
import android.content.*
import android.os.IBinder
import android.os.ResultReceiver
import android.view.Display
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.jack.nars.waver.MainActivity
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.ControlsRepository
import com.jack.nars.waver.data.LoopRepository
import com.jack.nars.waver.players.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SoundService : MediaBrowserService(), LifecycleOwner {
    var showVolumeAction = false
        private set

    var mediaSession: MediaSession? = null
        private set

    @Inject
    lateinit var loopsRepository: LoopRepository

    @Inject
    lateinit var controlsRepository: ControlsRepository


    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        Timber.i("SoundService created")
        Timber.d("ID: ${android.os.Process.myPid()}: ${android.os.Process.myTid()}")

        setupNotificationChannels(this)
        registerReceiver(mediaNotificationReceiver, MediaAction.filter())

        createMediaSession()
        sessionToken = mediaSession?.sessionToken

        setupPlayer()
    }


    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()

        Timber.i("SoundService destroyed")

        mediaSession?.isActive = false
        mediaSession?.release()

        playersMesh.release()

        unregisterReceiver(mediaNotificationReceiver)
    }


    private fun createMediaSession() {
        mediaSession = MediaSession(baseContext, "SoundService session").apply {
            setPlaybackState(
                PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)
                    .build()
            )

            setCallback(mediaSessionCallbacks)

            setSessionActivity(
                PendingIntent.getActivity(
                    this@SoundService,
                    0,
                    Intent(this@SoundService, MainActivity::class.java),
                    0
                )
            )
        }
    }


    private val mediaSessionCallbacks = object : MediaSession.Callback() {
        fun enterPlay(composition: CompositionData) {
            if (!composition.isPlayable) {
                Timber.w("Trying to EnterPlay with not playable composition")
                return
            }

            startService(Intent(this@SoundService, SoundService::class.java))

            mediaSession?.isActive = true
            playersMesh.play()
            mediaSession?.setPlaybackState(Builders.playState.build())
            mediaSession?.setMetadata(Builders.metadata(this@SoundService, composition).build())

            startForeground(
                ControlsNotificationBuilder.ID,
                ControlsNotificationBuilder(this@SoundService).build()
            )
            // TODO: consider register audio becoming noisy here
        }


        override fun onPlay() {
            Timber.i("Session: Play")
            if(playersMesh.composition == null)
                Timber.w("Trying to play with no composition")

            enterPlay(playersMesh.composition ?: return)
        }


        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Timber.i("Session: Play from Id")

            val composition = Json.decodeFromString<CompositionData>(mediaId ?: return)

            updateCurrentComposition(composition)

            enterPlay(composition)
        }


        override fun onPause() {
           Timber.i("Session: Pause")

            playersMesh.pause()
            this@SoundService.mediaSession?.setPlaybackState(Builders.pauseState.build())

            updateForegroundNotification()
            this@SoundService.stopForeground(false)
        }


        override fun onStop() {
            Timber.i("Session: Stop")

            playersMesh.pause()
            mediaSession?.setPlaybackState(Builders.stopState.build())

            stopSelf()
            mediaSession?.isActive = false
            stopForeground(true)
        }
    }


    private lateinit var playersMesh: PlayersMesh


    private fun setupPlayer() {
        playersMesh = PlayersMesh(this)

        loopsRepository.staticLoops.forEach {
            Timber.d(it.id)
            playersMesh.addLoop(it)
        }

        loopsRepository.activeCompositionData.observe(owner = this) {
            updateCurrentComposition(it)
        }

        controlsRepository.masterVolume.observe(owner = this) {
            updateMasterVolume(it)
        }
    }


    private fun updateCurrentComposition(c: CompositionData?) {
        playersMesh.updateComposition(c)

        if (c == null || !c.isPlayable)
            mediaSession!!.controller.transportControls.pause()
    }


    private fun updateMasterVolume(it: Float) {
        Timber.v("Updated master volume: $it")
        playersMesh.masterVolume = it
    }


    // ===============================================
    // ================= NOTIFICATIONS ===============
    // ===============================================
    private val mediaNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            with(intent?.action ?: "null intent") { Timber.d("Received: $this") }

            val controller = mediaSession?.controller.apply {
                if (this == null) Timber.w("null controller on notification button press")
            }

            when(intent?.action) {
                MediaAction.PLAY -> controller?.transportControls?.play()
                MediaAction.PAUSE -> controller?.transportControls?.pause()
                MediaAction.STOP -> controller?.transportControls?.stop()
                MediaAction.UP -> {

                }
                MediaAction.DOWN -> {

                }
            }
        }
    }


    private fun updateForegroundNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (nm.activeNotifications.any { it.id == ControlsNotificationBuilder.ID })
            nm.notify(
                ControlsNotificationBuilder.ID,
                ControlsNotificationBuilder(this).build()
            )
        else
            Timber.w("Trying to update non existing foreground notification")
    }


    // ===============================================
    // ================= UNUSED ======================
    // ===============================================
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("empty_media_root_id", null)
    }


    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        result.sendResult(null)
        return
    }


    override fun onBind(intent: Intent?): IBinder? {
        Timber.d("Bound to ?")
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }


    private val dispatcher = ServiceLifecycleDispatcher(this)

    override fun getLifecycle() = dispatcher.lifecycle
}

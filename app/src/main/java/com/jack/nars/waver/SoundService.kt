package com.jack.nars.waver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.util.Log

import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver

import android.service.media.MediaBrowserService

//import androidx.media.MediaBrowserServiceCompat
import android.media.browse.MediaBrowser
//import android.support.v4.media.MediaBrowserCompat
import android.media.session.MediaSession
//import android.support.v4.media.session.MediaSessionCompat
import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat
import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.os.IBinder
import android.os.ResultReceiver
import com.jack.nars.waver.sound.CompositionData
import com.jack.nars.waver.sound.LoopLoader
import com.jack.nars.waver.sound.players.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


const val TAG = "SOUND_SERVICE"

const val NOTIFICATION_ID_FOREGROUND = 1001
const val CHANNEL_ID_MEDIA_CONTROLS = "MEDIA_CONTROLS"

const val ACTION_MEDIA_PLAY = "com.jack.nars.waver.ACTION_MEDIA_PLAY"
const val ACTION_MEDIA_PAUSE = "com.jack.nars.waver.ACTION_MEDIA_PAUSE"
const val ACTION_MEDIA_STOP = "com.jack.nars.waver.ACTION_MEDIA_STOP"
const val ACTION_MEDIA_UP = "com.jack.nars.waver.ACTION_MEDIA_UP"
const val ACTION_MEDIA_DOWN = "com.jack.nars.waver.ACTION_MEDIA_DOWN"

const val COMMAND_MASTER_VOLUME = "COMMAND_MASTER_VOLUME"


class SoundService : MediaBrowserService() {
    private var mediaSession: MediaSession? = null


    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Service created")

        setupPlayer()

        setupNotificationChannels()

        registerReceiver(mediaNotificationReceiver, IntentFilter().apply {
            addAction(ACTION_MEDIA_PLAY)
            addAction(ACTION_MEDIA_PAUSE)
            addAction(ACTION_MEDIA_STOP)
            addAction(ACTION_MEDIA_UP)
            addAction(ACTION_MEDIA_DOWN)
        })

        mediaSession = MediaSession(baseContext, TAG).apply {
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

        sessionToken = mediaSession?.sessionToken
    }


    override fun onDestroy() {
        super.onDestroy()

        Log.i(TAG, "Service destroyed")

        mediaSession?.isActive = false
        mediaSession?.release()

        // TODO: release the player
        playersMesh.release()

        unregisterReceiver(mediaNotificationReceiver)
    }


    private val playStateBuilder = PlaybackState.Builder()
        .setActions(PlaybackState.ACTION_PAUSE
        or PlaybackState.ACTION_STOP
        or PlaybackState.ACTION_PLAY_PAUSE)
        .setState(
        PlaybackState.STATE_PLAYING,
        PlaybackState.PLAYBACK_POSITION_UNKNOWN,
        1f)


    private val pauseStateBuilder = PlaybackState.Builder()
        .setActions(PlaybackState.ACTION_PLAY
                or PlaybackState.ACTION_STOP
                or PlaybackState.ACTION_PLAY_PAUSE)
        .setState(
            PlaybackState.STATE_PAUSED,
            PlaybackState.PLAYBACK_POSITION_UNKNOWN,
            0f)


    private val stopStateBuilder = PlaybackState.Builder()
        .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)
        .setState(
            PlaybackState.STATE_STOPPED,
            PlaybackState.PLAYBACK_POSITION_UNKNOWN,
            0f)


    private fun metadataBuilder(composition: CompositionData) = MediaMetadata.Builder()
        .putString(MediaMetadata.METADATA_KEY_TITLE, "Brown Noise")
        .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, "Brown Noise")
        .putString(MediaMetadata.METADATA_KEY_ALBUM, getString(R.string.app_name))
        .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, getString(R.string.app_name))
        .putLong(MediaMetadata.METADATA_KEY_DURATION, -1)


    private val mediaSessionCallbacks = object : MediaSession.Callback() {
        fun enterPlay(composition: CompositionData) {
            startService(Intent(this@SoundService, SoundService::class.java))

            mediaSession?.isActive = true
            playersMesh.play()
            mediaSession?.setPlaybackState(playStateBuilder.build())
            mediaSession?.setMetadata(metadataBuilder(composition).build())

            startForeground(NOTIFICATION_ID_FOREGROUND, getForegroundNotificationBuilder().build())
            // TODO: consider register audio becoming noisy here
        }


        override fun onPlay() {
            Log.i(TAG, "Session: Play")
            if(playersMesh.composition == null)
                Log.w(TAG, "Trying to play with no compostion")

            enterPlay(playersMesh.composition ?: return)
        }


        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Log.i(TAG, "Session: Play from Id")

            val composition = Json.decodeFromString<CompositionData>(mediaId ?: return)

            playersMesh.updateComposition(composition)
            enterPlay(composition)
        }


        override fun onPause() {
            Log.i(TAG, "Session: Pause")

            playersMesh.pause()
            this@SoundService.mediaSession?.setPlaybackState(pauseStateBuilder.build())

            updateForegroundNotification()
            this@SoundService.stopForeground(false)
        }


        override fun onStop() {
            Log.i(TAG, "Session: Stop")

            playersMesh.pause()
            mediaSession?.setPlaybackState(stopStateBuilder.build())

            stopSelf()
            mediaSession?.isActive = false
            stopForeground(true)
        }


        override fun onCommand(command: String, args: Bundle?, cb: ResultReceiver?) {
            when(command) {
                COMMAND_MASTER_VOLUME -> args?.getFloat(null)?.let { playersMesh.masterVolume = it }
            }
        }
    }


    private lateinit var playersMesh: PlayersMesh


    private fun setupPlayer() {
        playersMesh = PlayersMesh(this)

        LoopLoader.getAllLoops(this).forEach {
            Log.d("LoopLoader", it.id)
            playersMesh.addLoop(it)
        }

//        val testComposition = CompositionData(loops = listOf(
//            CompositionItem("test:brown_noise", 0.5f),
//            CompositionItem("test:ambient_music", volume = 0.3f)
//        ))
//        playersMesh.updateComposition(testComposition)
    }


    // ===============================================
    // ================= NOTIFICATIONS ===============
    // ===============================================
    private val mediaNotificationReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received: " + (intent?.action ?: "null intent"))

            val controller = mediaSession?.controller.apply {
                if (this == null) Log.w(TAG, "null controller on notification button press")
            }

            when(intent?.action) {
                ACTION_MEDIA_PLAY -> controller?.transportControls?.play()
                ACTION_MEDIA_PAUSE -> controller?.transportControls?.pause()
                ACTION_MEDIA_STOP -> controller?.transportControls?.stop()
                ACTION_MEDIA_UP -> {

                }
                ACTION_MEDIA_DOWN -> {

                }
            }
        }
    }


    private fun setupNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID_MEDIA_CONTROLS,
                "Media Controls", // TODO: use resource string
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Desc"
                enableVibration(false)
            }
        )
    }


    private fun getForegroundNotificationBuilder(): Notification.Builder {
        val controller = mediaSession?.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description

        val playbackState  = controller?.playbackState?.state
        val isPlaying = playbackState == PlaybackState.STATE_PLAYING


        return Notification.Builder(this, CHANNEL_ID_MEDIA_CONTROLS).apply {
            // add playing info
            setContentTitle(description?.title) // TODO: find out what names to use
            setSubText(null) // TODO: ?
            setContentText(if (isPlaying) "Playing" else "Paused")  // TODO: use resource

            // add notification icons
            setSmallIcon(R.drawable.ic_notification)
            setLargeIcon(description?.iconBitmap) //TODO: add an image representing playback

            // add notification colors
            setColorized(true)
            setColor(ContextCompat.getColor(this@SoundService, R.color.colorAccent))

            // hide time and show on lock screen
            setShowWhen(false)
            setVisibility(Notification.VISIBILITY_PUBLIC)

            // notification click
            setContentIntent(controller?.sessionActivity)

            // notification swipe
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@SoundService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            fun addMediaAction(title: String, icon: Int, action: String) {
                addAction(
                    Notification.Action.Builder(
                        Icon.createWithResource(this@SoundService, icon),
                        title,
                        PendingIntent.getBroadcast(this@SoundService,
                            0,
                            Intent(action).setPackage(this@SoundService.packageName),
                            0
                        )
                    ).build()
                )
            }

            // notification buttons
            addMediaAction("Volume down", R.drawable.ic_notification_down, ACTION_MEDIA_DOWN)
            if (isPlaying)
                addMediaAction("Pause", R.drawable.ic_notification_pause, ACTION_MEDIA_PAUSE)
            else
                addMediaAction("Play", R.drawable.ic_notification_play, ACTION_MEDIA_PLAY)
            addMediaAction("Volume up", R.drawable.ic_notification_up, ACTION_MEDIA_UP)
            addMediaAction("Close", R.drawable.ic_notification_close, ACTION_MEDIA_STOP)

            // Take advantage of MediaStyle features
            @Suppress("RemoveRedundantSpreadOperator")
            style = Notification.MediaStyle()
                .setMediaSession(this@SoundService.mediaSession?.sessionToken)
                .setShowActionsInCompactView(*intArrayOf(0, 1, 2))

            Log.d(TAG, "MediaSession token to notification: %s".format(this@SoundService.mediaSession?.sessionToken))
        }
    }


    private fun updateForegroundNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_FOREGROUND, getForegroundNotificationBuilder().build())
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
        Log.i(TAG, "Bound to ?")

        return super.onBind(intent)
    }
}

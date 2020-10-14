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


const val TAG = "SOUND_SERVICE"

const val NOTIFICATION_ID_FOREGROUND = 1001
const val CHANNEL_ID_MEDIA_CONTROLS = "MEDIA_CONTROLS"

const val ACTION_MEDIA_PLAY = "com.jack.nars.waver.ACTION_MEDIA_PLAY"
const val ACTION_MEDIA_PAUSE = "com.jack.nars.waver.ACTION_MEDIA_PAUSE"
const val ACTION_MEDIA_STOP = "com.jack.nars.waver.ACTION_MEDIA_STOP"


class SoundService : MediaBrowserService() {
    private var mediaSession: MediaSession? = null


    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Service created")

        setupNotificationChannels()

        registerReceiver(mediaNotificationReceiver, IntentFilter().apply {
            addAction(ACTION_MEDIA_PLAY)
            addAction(ACTION_MEDIA_PAUSE)
            addAction(ACTION_MEDIA_STOP)
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


    private val mediaSessionCallbacks = object : MediaSession.Callback() {
        override fun onPlay() {
            Log.i(TAG, "Session: Play")

            startService(Intent(this@SoundService, SoundService::class.java))
            // Set the session active  (and update metadata and state)

            mediaSession?.isActive = true

            // TODO: start the player

            mediaSession?.setPlaybackState(PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PAUSE
                        or PlaybackState.ACTION_STOP
                        or PlaybackState.ACTION_PLAY_PAUSE)
                .setState(
                    PlaybackState.STATE_PLAYING,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    1f)
                .build())

            mediaSession?.setMetadata(MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "Test Title")
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, "Test Title!")
                .putLong(MediaMetadata.METADATA_KEY_DURATION, -1)
                .build()
            )

            // TODO: consider register audio becoming noisy here

            startForeground(NOTIFICATION_ID_FOREGROUND, getForegroundNotificationBuilder().build())
        }


        override fun onPause() {
            Log.i(TAG, "Session: Pause")

            // TODO: pause the player

            this@SoundService.mediaSession?.setPlaybackState(PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY
                        or PlaybackState.ACTION_STOP
                        or PlaybackState.ACTION_PLAY_PAUSE)
                .setState(
                    PlaybackState.STATE_PAUSED,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    0f)
                .build())

            this@SoundService.stopForeground(false)
        }


        override fun onStop() {
            Log.i(TAG, "Session: Stop")

            // TODO: stop the player

            mediaSession?.setPlaybackState(PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)
                .setState(
                    PlaybackState.STATE_STOPPED,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    0f)
                .build())

            stopSelf()
            mediaSession?.isActive = false
            stopForeground(false)
        }
    }


    private val mediaNotificationReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "Received: " + (intent?.action ?: "null intent"))
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

        return Notification.Builder(this, CHANNEL_ID_MEDIA_CONTROLS).apply {
            // add playing info
            setContentTitle("Content Title") // TODO: find out what names to use
            setContentText("Content Text") // TODO: ?
            setSubText("Sub Text")  // TODO: ?

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

            // pause action
            addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this@SoundService, R.drawable.ic_notification_pause),
                    "Pause",
                    PendingIntent.getBroadcast(this@SoundService,
                        0,
                        Intent(ACTION_MEDIA_PAUSE).setPackage(this@SoundService.packageName),
                        0
                    )
                ).build()
            )

            // close action
            addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this@SoundService, R.drawable.ic_notification_close),
                    "Stop",
                    PendingIntent.getBroadcast(this@SoundService,
                        0,
                        Intent(ACTION_MEDIA_STOP).setPackage(this@SoundService.packageName),
                        0
                    )
//                  MediaButtonReceiver.buildMediaButtonPendingIntent(this@SoundService, PlaybackStateCompat.ACTION_STOP)
                ).build()
            )

            // Take advantage of MediaStyle features
            style = Notification.MediaStyle()
                .setMediaSession(this@SoundService.mediaSession?.sessionToken)
                .setShowActionsInCompactView(0)

            Log.d(TAG, "MediaSession token to notification: %s".format(this@SoundService.mediaSession?.sessionToken))
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        Log.i(TAG, "Service destroyed")

        mediaSession?.isActive = false
        mediaSession?.release()

        unregisterReceiver(mediaNotificationReceiver)
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
}

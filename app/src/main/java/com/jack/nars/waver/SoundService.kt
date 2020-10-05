package com.jack.nars.waver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver


const val TAG = "SOUND_SERVICE"
const val NOTIFICATION_ID = 1
const val CHANNEL_ID = "WAVER"

class SoundService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
//    private var sessionToken: MediaSessionCompat.Token? = null




    private fun createNotification(): Notification {
        val controller = mediaSession?.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description


        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            // Add the metadata for the currently playing track
            setContentTitle("Hello")
            setContentText("Test")
            setSubText("SubTest")
//            setLargeIcon(description?.iconBitmap)

            // Enable launching the player by clicking the notification
            setContentIntent(controller?.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@SoundService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Make the transport controls visible on the lock-screen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(R.drawable.ic_notifications_black_24dp)
            color = ContextCompat.getColor(this@SoundService, R.color.colorAccent)

            // Add a pause button
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_dashboard_black_24dp,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@SoundService,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@SoundService,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }

        return builder.build()
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Service created")


        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, TAG).apply {
            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )

            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    Log.d(TAG, "Session Play")

//                    val am = this@SoundService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    startService(Intent(this@SoundService, SoundService::class.java))
                    // Set the session active  (and update metadata and state)

                    this@SoundService.mediaSession?.isActive = true
                    // start the player (custom call)
//                    player.start()
                    // Register BECOME_NOISY BroadcastReceiver
//                    registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                    // Put the service in the foreground, post notification
                    this@SoundService.startForeground(NOTIFICATION_ID, createNotification())
                }

                override fun onPause() {
                    Log.d(TAG, "Session Pause")

                    this@SoundService.stopForeground(false)
                }

                override fun onStop() {
                    Log.d(TAG, "Session Stop")

                    this@SoundService.stopSelf()
                    this@SoundService.mediaSession?.isActive = false
                    this@SoundService.stopForeground(false)
                }
            })

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
//            this@SoundService.sessionToken = sessionToken
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Media Controls",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.description = "Desc"
        //        chan.lightColor = Color.BLUE
        //        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("empty_media_root_id", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
        return
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return super.onStartCommand(intent, flags, startId)
//    }

//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }


    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "Service destroyed")
    }

}

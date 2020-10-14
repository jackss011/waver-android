package com.jack.nars.waver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.util.Log

import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver

import android.service.media.MediaBrowserService as MediaBrowserService_Start
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
import android.graphics.Color
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.provider.MediaStore

//import androidx.core.app.NotificationCompat


const val TAG = "SOUND_SERVICE"

const val NOTIFICATION_ID_MEDIA_CONTROLS = 1
const val CHANNEL_ID_MEDIA_CONTROLS = "MEDIA_CONTROLS"

const val ACTION_MEDIA_PLAY = "com.jack.nars.waver.ACTION_MEDIA_PLAY"
const val ACTION_MEDIA_PAUSE = "com.jack.nars.waver.ACTION_MEDIA_PAUSE"
const val ACTION_MEDIA_STOP = "com.jack.nars.waver.ACTION_MEDIA_STOP"

class SoundService : MediaBrowserService() {

    private var mediaSession: MediaSession? = null
    private lateinit var stateBuilder: PlaybackState.Builder
//    private var sessionToken: MediaSessionCompat.Token? = null




    private fun createNotification(): Notification {
        val controller = mediaSession?.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description


        val builder = Notification.Builder(this, CHANNEL_ID_MEDIA_CONTROLS).apply {
            // Add the metadata for the currently playing track
            setContentTitle(getString(R.string.app_name))
            setContentText("Test")
            setSubText("SubTest")
            setLargeIcon(description?.iconBitmap)

            setShowWhen(false)

            if (controller == null)
                Log.e(TAG, "Controller is not with us")
            else {
                Log.i(TAG, "Controller is fine: %s".format(controller.sessionActivity?.javaClass.toString()))

            }

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
            setVisibility(Notification.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(R.drawable.ic_notification)
//            color = ContextCompat.getColor(this@SoundService, R.color.colorAccent)

            // Add a pause button
            val pauseIntent = PendingIntent.getBroadcast(this@SoundService,
                0,
                Intent(ACTION_MEDIA_PLAY).setPackage(this@SoundService.packageName),
                0
            )


//                MediaButtonReceiver.buildMediaButtonPendingIntent(
//                    this@SoundService,
//                    ComponentName(this@SoundService, this@SoundService::class.java),
//                    PlaybackStateCompat.ACTION_PLAY_PAUSE
//                )

            addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this@SoundService, R.drawable.ic_dashboard_black_24dp),
                    "Pause",
                    pauseIntent
                ).build()
            )

            val activityIntent = Intent(this@SoundService, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this@SoundService,
                0,
                activityIntent,
                0)

            addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this@SoundService, R.drawable.ic_home_black_24dp),
                    "Stop",
                    pendingIntent
                ).build()
            )

            Log.d(TAG, "MediaSession token to notification: %s".format(this@SoundService.mediaSession?.sessionToken))

            // Take advantage of MediaStyle features
            style = Notification.MediaStyle()
                .setMediaSession(this@SoundService.mediaSession?.sessionToken)
                .setShowActionsInCompactView(0)

            setColorized(true)
            setColor(ContextCompat.getColor(this@SoundService, R.color.colorAccent))
        }

        return builder.build()
    }



    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Service created")


        // Create a MediaSessionCompat
        mediaSession = MediaSession(baseContext, TAG).apply {
            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            Log.d(TAG, "controller: %s".format(controller.toString()))

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY
                            or PlaybackState.ACTION_PLAY_PAUSE
                )

            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    Log.d(TAG, "Session Play")

//                    val am = this@SoundService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    startService(Intent(this@SoundService, SoundService::class.java))
                    // Set the session active  (and update metadata and state)

                    this@apply.isActive = true
                    // start the player (custom call)
//                    player.start()

                    this@SoundService.mediaSession?.setPlaybackState(PlaybackState.Builder()
                        .setActions(PlaybackState.ACTION_PAUSE
                                or PlaybackState.ACTION_STOP
                                or PlaybackState.ACTION_PLAY_PAUSE)
                        .setState(
                            PlaybackState.STATE_PLAYING,
                            PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                        1f)
                        .build())

                    this@SoundService.mediaSession?.setMetadata(MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, "Test Title")
                        .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, "Test Title!")
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, -1)
                        .build()
                        )
                    // Register BECOME_NOISY BroadcastReceiver
//                    registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                    // Put the service in the foreground, post notification
                    this@SoundService.startForeground(NOTIFICATION_ID_MEDIA_CONTROLS, createNotification())
                }


                override fun onPause() {
                    Log.d(TAG, "Session Pause")

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
                    Log.d(TAG, "Session Stop")

                    this@SoundService.mediaSession?.setPlaybackState(PlaybackState.Builder()
                        .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)
                        .setState(
                            PlaybackState.STATE_STOPPED,
                            PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                            0f)
                        .build())

                    this@SoundService.stopSelf()
                    this@SoundService.mediaSession?.isActive = false
                    this@SoundService.stopForeground(false)
                }

//                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
//                    Log.d(TAG, "Media Button Event")
//
//                    return false
//                }
            })

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
//            this@SoundService.sessionToken = sessionToken

            val activityIntent = Intent(this@SoundService, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this@SoundService,
                0,
                activityIntent,
                0)

            setSessionActivity(pendingIntent)
        }


        val channel = NotificationChannel(
            CHANNEL_ID_MEDIA_CONTROLS,
            "Media Controls",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.description = "Desc"
        channel.enableVibration(false)
//        channel.lightColor = Color.CYAN
        //        chan.lightColor = Color.BLUE
        //        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)



//        val actionIntent = Intent(ACTION_MEDIA_TEST)
        val actionIntentFilter = IntentFilter(ACTION_MEDIA_PLAY)

        actionReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                Log.d(TAG, "Received")
            }
        }

        registerReceiver(actionReceiver, actionIntentFilter)
    }

    private lateinit var actionReceiver: BroadcastReceiver


    // ============================================================
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


    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "Service destroyed")
        mediaSession?.isActive = false
        mediaSession?.release()

        unregisterReceiver(actionReceiver)

    }

}

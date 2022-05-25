package com.jack.nars.waver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.jack.nars.waver.R
import timber.log.Timber


object MediaAction {
    const val PLAY = "com.jack.nars.waver.service.ACTION_MEDIA_PLAY"
    const val PAUSE = "com.jack.nars.waver.service.ACTION_MEDIA_PAUSE"
    const val STOP = "com.jack.nars.waver.service.ACTION_MEDIA_STOP"
    const val UP = "com.jack.nars.waver.service.ACTION_MEDIA_UP"
    const val DOWN = "com.jack.nars.waver.service.ACTION_MEDIA_DOWN"

    fun filter() = IntentFilter().apply {
        addAction(PLAY)
        addAction(PAUSE)
        addAction(STOP)
        addAction(UP)
        addAction(DOWN)
    }
}


const val CHANNEL_ID_MEDIA_CONTROLS = "MEDIA_CONTROLS"



fun setupNotificationChannels(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    nm.createNotificationChannel(
        NotificationChannel(
            CHANNEL_ID_MEDIA_CONTROLS,
            "Media Controls", // TODO: use resource string
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Desc" // TODO: use resource string
            enableVibration(false)
            setSound(null, null)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
    )
}


class ControlsNotificationBuilder(val context: SoundService)
    : Notification.Builder(context, CHANNEL_ID_MEDIA_CONTROLS) {

    companion object {
        const val ID = 1001
    }


    init {
        val controller = context.mediaSession?.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description

        val playbackState  = controller?.playbackState?.state
        val isPlaying = playbackState == PlaybackState.STATE_PLAYING


        // add playing info
        setContentTitle(description?.title) // TODO: find out what names to use
        setSubText(null) // TODO: ?
        setContentText(if (isPlaying) "Playing" else "Paused")  // TODO: use resource
        setCategory(Notification.CATEGORY_TRANSPORT)

        // add notification icons
        setSmallIcon(R.drawable.ic_notification)
        setLargeIcon(description?.iconBitmap) //TODO: add an image representing playback

        // add notification colors
        setColorized(true)
        setColor(ContextCompat.getColor(context, R.color.colorAccent))

        // hide time and show on lock screen
        setShowWhen(false)
        setVisibility(Notification.VISIBILITY_PUBLIC)

        // notification click
        setContentIntent(controller?.sessionActivity)

        // notification swipe
        setDeleteIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_STOP
            )
        )

        fun addMediaAction(title: String, icon: Int, action: String) {
            addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, icon),
                    title,
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(action).setPackage(context.packageName),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
            )
        }

        val showVolume = context.showVolumeAction

        // notification buttons
        if (showVolume)
            addMediaAction("Volume down", R.drawable.ic_notification_down, MediaAction.DOWN)

        if (isPlaying)
            addMediaAction("Pause", R.drawable.ic_notification_pause, MediaAction.PAUSE)
        else
            addMediaAction("Play", R.drawable.ic_notification_play, MediaAction.PLAY)

        if (showVolume)
            addMediaAction("Volume up", R.drawable.ic_notification_up, MediaAction.UP)

        addMediaAction("Close", R.drawable.ic_notification_close, MediaAction.STOP)

        // Take advantage of MediaStyle features
        @Suppress("RemoveRedundantSpreadOperator")
        style = Notification.MediaStyle()
            .setMediaSession(context.mediaSession?.sessionToken)
            .setShowActionsInCompactView(*(if (showVolume) intArrayOf(0, 1, 2) else intArrayOf(0)))

        Timber.d("MediaSession token to notification: %s".format(context.mediaSession?.sessionToken))
    }
}
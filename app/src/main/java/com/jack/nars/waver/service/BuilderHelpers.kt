package com.jack.nars.waver.service

import android.content.Context
import android.media.MediaMetadata
import android.media.session.PlaybackState
import com.jack.nars.waver.R
import com.jack.nars.waver.sound.CompositionData


object Builders {
    val playState: PlaybackState.Builder = PlaybackState.Builder()
        .setActions(
        PlaybackState.ACTION_PAUSE
        or PlaybackState.ACTION_STOP
        or PlaybackState.ACTION_PLAY_PAUSE)
        .setState(
        PlaybackState.STATE_PLAYING,
        PlaybackState.PLAYBACK_POSITION_UNKNOWN,
        1f)

    val pauseState: PlaybackState.Builder = PlaybackState.Builder()
        .setActions(
            PlaybackState.ACTION_PLAY
                    or PlaybackState.ACTION_STOP
                    or PlaybackState.ACTION_PLAY_PAUSE)
        .setState(
            PlaybackState.STATE_PAUSED,
            PlaybackState.PLAYBACK_POSITION_UNKNOWN,
            0f)

    val stopState: PlaybackState.Builder = PlaybackState.Builder()
        .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)
        .setState(
            PlaybackState.STATE_STOPPED,
            PlaybackState.PLAYBACK_POSITION_UNKNOWN,
            0f)

    fun metadata(context: Context, composition: CompositionData):
            MediaMetadata.Builder = MediaMetadata.Builder()
        .putString(MediaMetadata.METADATA_KEY_TITLE, "Brown Noise")
        .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, "Brown Noise")
        .putString(MediaMetadata.METADATA_KEY_ALBUM, context.getString(R.string.app_name))
        .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, context.getString(R.string.app_name))
        .putLong(MediaMetadata.METADATA_KEY_DURATION, -1)
}

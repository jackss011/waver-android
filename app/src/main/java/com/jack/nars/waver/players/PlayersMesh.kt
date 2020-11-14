package com.jack.nars.waver.players

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.Loop


class PlayersMesh(val context: Context) {
    val TAG = "PlayersMesh"


    private val loops = mutableMapOf<String, Loop>()
    private val players = mutableMapOf<String, BasePlayer>()

    var composition: CompositionData? = null
        private set

    var isPlaying = false
        private set


    fun addLoop(loop: Loop) {
        if (loops.containsKey(loop.id)) return

//        val player = when(loop.mode) {
//            Loop.Mode.CROSSFADE -> CrossfadeLoopPlayer(context)
//            Loop.Mode.SEAMLESS -> SeamlessLoopPlayer(context)
//        }
        val player = CrossfadeLoopPlayer(context)

        loops[loop.id] = loop
        players[loop.id] = player

        player.prepare { context: Context, mp: MediaPlayer ->
            mp.setDataSource(context, Uri.parse(loop.uri))
        }
    }


    fun play() {
        (composition ?: return).loops.map { it.id }.forEach {
            players[it]?.play()
            Log.d(TAG, "Playing 1")
        }
        isPlaying = true
    }


    fun pause() {
        isPlaying = false

        players.values.forEach { it.pause() }
    }


    fun release() {
        isPlaying = false

        players.values.forEach { it.release() }
    }


    fun updateComposition(new: CompositionData?) {
//        new.loops.forEach { if(it.id !in players.keys) Log.e("MeshPlayer", "Missing loop")}

        Log.d(TAG, "Composition:")
        new?.loops?.forEach { Log.i(TAG, it.id) }

        Log.d(TAG, "Players:")
        players.keys.forEach { Log.i(TAG, it) }


        // copy old list
        val old = this.composition
        this.composition = new

        // if new composition is null stop all players and return
        if (new == null) {
            pause()
            return
        }

        // remove old loops
        if (old != null) {
            val oldIds = old.loops.map { it.id }
            val newIds = new.loops.map { it.id }
            val removed = (oldIds - newIds)
            removed.forEach { players[it]?.pause() }
        }

        // set all new loop's volume
        updateVolumes()

        // play all new loops
        if (isPlaying) new.loops.forEach { players[it.id]?.play() }
    }


    private fun updateVolumes() {
        (this.composition ?: return).loops.forEach {
            players[it.id]?.volume = it.volume * masterVolume
        }
    }


    var masterVolume = 1f
        set(value) {
            field = value

            updateVolumes()
        }
}
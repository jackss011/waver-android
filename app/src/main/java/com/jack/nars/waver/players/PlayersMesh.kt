package com.jack.nars.waver.players

class PlayersMesh {
    private val loops = mutableMapOf<String, Loop>()
    private val players = mutableMapOf<String, Player>()

    private var composition: CompositionData? = null

    var isPlaying = false
        private set


    fun addLoop(loop: Loop) {
        if (loops.containsKey(loop.id)) return

        val player = when(loop.mode) {
            Loop.Mode.CROSSFADE -> CrossfadeLoopPlayer()
            Loop.Mode.SEAMLESS -> SeamlessLoopPlayer()
        }

        loops[loop.id] = loop
        players[loop.id] = player

        player.prepare(loop.source)
    }


    fun play() {
        (composition ?: return).loops.map { it.id }.forEach { players[it]?.play() }
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


    fun updateComposition(new: CompositionData) {
        // copy old list
        val old = (this.composition ?: return)
        this.composition = new

        // remove old loops
        val oldIds = old.loops.map { it.id }
        val newIds = new.loops.map { it.id }
        val removed = (oldIds - newIds)
        removed.forEach { players[it]?.pause() }

        // set all new loop's volume
        new.loops.forEach { players[it.id]?.volume = it.volume}

        // play all new loops
        if (isPlaying) new.loops.forEach { players[it.id]?.play() }
    }
}
package com.jack.nars.waver.sound

import com.jack.nars.waver.R


object Palette {
    enum class LoopMode {
        SEAMLESS,
        BLEND,
    }


    data class Sound(
        val id: String,
        val title: String,
        val res: Int,
        val mode: LoopMode
    )


    val sounds = mapOf(
        "test_brown_noise" to Sound(
            "test_brown_noise",
            "Brown Noise",
            R.raw.brown_noise,
            LoopMode.SEAMLESS
        ),
        "test_music" to Sound(
            "test_music",
            "Music",
            R.raw.ambient_loop,
            LoopMode.BLEND
        )
    )


    fun soundOfId(id: String): Sound? {
        return sounds[id]
    }
}
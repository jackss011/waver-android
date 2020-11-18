package com.jack.nars.waver.players

import android.content.Context


class WhiteNoisePlayer(context: Context) : BasePlayer(context) {

    var colorValue: Float = 0.5f
        private set(v) {
            field = v.coerceIn(-1f, 1f)
        }


    override fun play() {
    }


    override fun pause() {
    }


    override fun release() {
    }
}
package com.jack.nars.waver.sound


class Composition() {
    data class Item(val soundId: String, val volume: Float)


    val items = mutableMapOf<String, Item>()
    var volume: Float = 1f


    fun addItem(soundId: String, volume: Float = 1f) {
        items[soundId] = Item(soundId, volume)
    }


    fun addItem(sound: Palette.Sound, volume: Float = 1f) {
        items[sound.id] = Item(sound.id, volume)
    }
}

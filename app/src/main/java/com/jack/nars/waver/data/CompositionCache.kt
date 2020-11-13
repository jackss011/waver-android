package com.jack.nars.waver.data

class CompositionCache {
    private val map = mutableMapOf<String, CompositionItem>()

    fun saveLoop(item: CompositionItem) {
        map[item.id] = item
    }

    fun retrieveLoop(id: String): CompositionItem? {
        return map[id]
    }
}
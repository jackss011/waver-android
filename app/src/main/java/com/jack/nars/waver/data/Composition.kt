package com.jack.nars.waver.data

import kotlinx.serialization.Serializable

@Serializable
data class CompositionItem(val id: String, val volume: Float)

@Serializable
data class CompositionData(val loops: List<CompositionItem>)
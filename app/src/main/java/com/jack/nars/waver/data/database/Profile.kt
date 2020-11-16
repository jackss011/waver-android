package com.jack.nars.waver.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity
data class Profile(
    @PrimaryKey(autoGenerate = true) val idProfile: Long = 0,
    val name: String,
    val creationTimestamp: Long,
)


@Entity(primaryKeys = ["idProfile", "idLoop"])
data class LoopInProfile(
    val idProfile: Long,
    val idLoop: String,
    val intensity: Float,
)


data class ProfileWithItems(
    @Embedded val profile: Profile,
    @Relation(parentColumn = "idProfile", entityColumn = "idProfile")
    val loops: List<LoopInProfile>
)
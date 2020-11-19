package com.jack.nars.waver.data.database

import androidx.room.*
import androidx.room.ForeignKey.CASCADE


@Entity
data class Profile(
    @PrimaryKey(autoGenerate = true) val idProfile: Long = 0,
    val name: String,
    val creationTimestamp: Long,
)


@Entity(
    primaryKeys = ["idProfile", "idLoop"],
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["idProfile"],
            childColumns = ["idProfile"],
            onDelete = CASCADE,
            onUpdate = CASCADE,
        ),
    ],
)
data class LoopInProfile(
    val idProfile: Long = 0,
    val idLoop: String,
    val intensity: Float,
)


data class ProfileWithItems(
    @Embedded val profile: Profile,
    @Relation(parentColumn = "idProfile", entityColumn = "idProfile")
    val loops: List<LoopInProfile>
)
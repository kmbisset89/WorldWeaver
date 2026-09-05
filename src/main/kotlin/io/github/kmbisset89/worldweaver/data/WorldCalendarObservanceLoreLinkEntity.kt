package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "world_calendar_observance_lore",
    primaryKeys = ["observanceId", "loreId"],
    foreignKeys = [
        ForeignKey(
            entity = WorldCalendarObservanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["observanceId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["loreId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("observanceId"),
        Index("loreId"),
    ],
)
internal data class WorldCalendarObservanceLoreLinkEntity(
    val observanceId: String,
    val loreId: String,
)

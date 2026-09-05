package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "world_calendar_observances",
    foreignKeys = [
        ForeignKey(
            entity = WorldEntity::class,
            parentColumns = ["id"],
            childColumns = ["worldId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("worldId"),
        Index("monthId"),
    ],
)
internal data class WorldCalendarObservanceEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val name: String,
    val notes: String,
    val kind: String,
    val monthId: String,
    val day: Int,
    val year: Int?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "world_calendars",
    foreignKeys = [
        ForeignKey(
            entity = WorldEntity::class,
            parentColumns = ["id"],
            childColumns = ["worldId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["worldId"], unique = true)],
)
internal data class WorldCalendarEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val eraSuffix: String,
    val currentYear: Int?,
    val currentMonthId: String?,
    val currentDay: Int?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

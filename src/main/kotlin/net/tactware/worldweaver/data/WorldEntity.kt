package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worlds")
internal data class WorldEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val defaultGameSystem: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "campaigns",
    foreignKeys = [
        ForeignKey(
            entity = WorldEntity::class,
            parentColumns = ["id"],
            childColumns = ["worldId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("worldId")],
)
internal data class CampaignEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val name: String,
    val description: String,
    val notes: String,
    val gameSystem: String?,
    val status: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

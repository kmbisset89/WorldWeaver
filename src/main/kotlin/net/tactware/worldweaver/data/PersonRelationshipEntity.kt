package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_relationships",
    foreignKeys = [
        ForeignKey(
            entity = FactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["factionId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("fromId"),
        Index("toId"),
        Index("factionId"),
    ],
)
internal data class PersonRelationshipEntity(
    @PrimaryKey val id: String,
    val fromKind: String,
    val fromId: String,
    val toKind: String,
    val toId: String,
    val type: String,
    val description: String,
    val factionId: String?,
)

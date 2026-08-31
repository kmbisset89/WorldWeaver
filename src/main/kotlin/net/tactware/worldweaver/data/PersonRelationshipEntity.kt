package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_relationships",
    indices = [
        Index("fromId"),
        Index("toId"),
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
    val factionLean: String,
)

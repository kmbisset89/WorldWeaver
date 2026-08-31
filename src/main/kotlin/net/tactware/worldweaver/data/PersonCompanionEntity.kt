package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_companions",
    indices = [
        Index("ownerId"),
        Index("companionId"),
        Index(
            value = ["ownerKind", "ownerId", "companionKind", "companionId"],
            unique = true,
        ),
    ],
)
internal data class PersonCompanionEntity(
    @PrimaryKey val id: String,
    val ownerKind: String,
    val ownerId: String,
    val companionKind: String,
    val companionId: String,
    val type: String,
)

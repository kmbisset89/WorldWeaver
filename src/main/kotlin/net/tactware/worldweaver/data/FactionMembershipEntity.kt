package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "faction_memberships",
    foreignKeys = [
        ForeignKey(
            entity = FactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["factionId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("factionId"),
        Index("personKind", "personId"),
        Index(value = ["personKind", "personId", "factionId"], unique = true),
    ],
)
internal data class FactionMembershipEntity(
    @PrimaryKey val id: String,
    val personKind: String,
    val personId: String,
    val factionId: String,
    val role: String,
    val notes: String,
    val createdAtEpochMillis: Long,
)

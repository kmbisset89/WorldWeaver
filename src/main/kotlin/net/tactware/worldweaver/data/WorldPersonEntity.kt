package net.tactware.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "world_people",
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
    ],
)
internal data class WorldPersonEntity(
    @PrimaryKey val id: String,
    val worldId: String,
    val kind: String,
    val name: String,
    val description: String,
    val race: String,
    val classLevels: String,
    val abilities: String,
    val hitPoints: Int,
    val maxHitPoints: Int,
    val temporaryHitPoints: Int,
    val armorClass: Int,
    val walkSpeed: Int,
    val deathSaves: String,
    val items: String,
    val features: String,
    val spells: String,
    val notes: String,
    val skills: String = "",
    val spellSlots: String = "",
    val concentratingSpell: String = "",
    val creatureSize: String = "Medium",
    val sheetSystem: String,
    val pf2ePayload: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

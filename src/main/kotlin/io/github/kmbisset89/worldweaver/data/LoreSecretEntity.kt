package io.github.kmbisset89.worldweaver.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lore_secrets",
    foreignKeys = [
        ForeignKey(
            entity = LoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["loreId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("loreId")],
)
internal data class LoreSecretEntity(
    @PrimaryKey val id: String,
    val loreId: String,
    val title: String,
    val secret: String,
    val sortIndex: Int,
)

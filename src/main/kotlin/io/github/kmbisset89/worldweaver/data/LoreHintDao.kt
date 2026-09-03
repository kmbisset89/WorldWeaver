package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LoreHintDao {
    @Query("SELECT * FROM lore_hints WHERE secretId = :secretId ORDER BY sortIndex ASC")
    suspend fun getBySecret(secretId: String): List<LoreHintEntity>

    @Query(
        """
        SELECT hints.* FROM lore_hints AS hints
        INNER JOIN lore_secrets AS secrets ON secrets.id = hints.secretId
        INNER JOIN lore ON lore.id = secrets.loreId
        WHERE lore.worldId = :worldId
        ORDER BY hints.secretId ASC, hints.sortIndex ASC
        """
    )
    fun observeByWorld(worldId: String): Flow<List<LoreHintEntity>>

    @Query(
        """
        SELECT hints.* FROM lore_hints AS hints
        INNER JOIN lore_secrets AS secrets ON secrets.id = hints.secretId
        INNER JOIN lore ON lore.id = secrets.loreId
        WHERE lore.worldId = :worldId
        ORDER BY hints.secretId ASC, hints.sortIndex ASC
        """
    )
    suspend fun getByWorld(worldId: String): List<LoreHintEntity>

    @Insert
    suspend fun insertAll(entities: List<LoreHintEntity>)

    @Query(
        """
        DELETE FROM lore_hints WHERE secretId IN (
            SELECT id FROM lore_secrets WHERE loreId = :loreId
        )
        """
    )
    suspend fun deleteByLore(loreId: String)
}

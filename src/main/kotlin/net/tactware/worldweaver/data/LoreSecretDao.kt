package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LoreSecretDao {
    @Query("SELECT * FROM lore_secrets WHERE loreId = :loreId ORDER BY sortIndex ASC")
    fun observeByLore(loreId: String): Flow<List<LoreSecretEntity>>

    @Query("SELECT * FROM lore_secrets WHERE loreId = :loreId ORDER BY sortIndex ASC")
    suspend fun getByLore(loreId: String): List<LoreSecretEntity>

    @Query(
        """
        SELECT secrets.* FROM lore_secrets AS secrets
        INNER JOIN lore ON lore.id = secrets.loreId
        WHERE lore.worldId = :worldId
        ORDER BY secrets.loreId ASC, secrets.sortIndex ASC
        """
    )
    fun observeByWorld(worldId: String): Flow<List<LoreSecretEntity>>

    @Query(
        """
        SELECT secrets.* FROM lore_secrets AS secrets
        INNER JOIN lore ON lore.id = secrets.loreId
        WHERE lore.worldId = :worldId
        ORDER BY secrets.loreId ASC, secrets.sortIndex ASC
        """
    )
    suspend fun getByWorld(worldId: String): List<LoreSecretEntity>

    @Insert
    suspend fun insertAll(entities: List<LoreSecretEntity>)

    @Query("DELETE FROM lore_secrets WHERE loreId = :loreId")
    suspend fun deleteByLore(loreId: String)
}

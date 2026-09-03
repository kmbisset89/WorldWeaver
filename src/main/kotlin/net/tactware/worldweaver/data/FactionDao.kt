package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FactionDao {
    @Query("SELECT * FROM factions WHERE worldId = :worldId ORDER BY name ASC")
    fun observeByWorld(worldId: String): Flow<List<FactionEntity>>

    @Query("SELECT * FROM factions WHERE worldId = :worldId ORDER BY name ASC")
    suspend fun getByWorld(worldId: String): List<FactionEntity>

    @Query("SELECT * FROM factions WHERE id = :id")
    suspend fun getById(id: String): FactionEntity?

    @Query(
        """
        SELECT * FROM factions
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR goals LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<FactionEntity>

    @Insert
    suspend fun insert(entity: FactionEntity)

    @Update
    suspend fun update(entity: FactionEntity)

    @Query("DELETE FROM factions WHERE id = :id")
    suspend fun delete(id: String)
}

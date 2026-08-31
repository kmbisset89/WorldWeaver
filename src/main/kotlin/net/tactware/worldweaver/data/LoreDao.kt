package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LoreDao {
    @Query("SELECT * FROM lore WHERE worldId = :worldId ORDER BY title ASC")
    fun observeByWorld(worldId: String): Flow<List<LoreEntity>>

    @Query("SELECT * FROM lore WHERE worldId = :worldId ORDER BY title ASC")
    suspend fun getByWorld(worldId: String): List<LoreEntity>

    @Query("SELECT * FROM lore WHERE id = :id")
    suspend fun getById(id: String): LoreEntity?

    @Query(
        """
        SELECT * FROM lore
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY title ASC
        """
    )
    suspend fun searchLike(query: String): List<LoreEntity>

    @Insert
    suspend fun insert(entity: LoreEntity)

    @Update
    suspend fun update(entity: LoreEntity)

    @Query("DELETE FROM lore WHERE id = :id")
    suspend fun delete(id: String)
}

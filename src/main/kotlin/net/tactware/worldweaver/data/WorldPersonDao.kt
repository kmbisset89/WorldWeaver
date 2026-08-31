package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldPersonDao {
    @Query("SELECT * FROM world_people WHERE worldId = :worldId ORDER BY name ASC")
    fun observeByWorld(worldId: String): Flow<List<WorldPersonEntity>>

    @Query("SELECT COUNT(*) FROM world_people")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM world_people WHERE worldId = :worldId ORDER BY name ASC")
    suspend fun getByWorld(worldId: String): List<WorldPersonEntity>

    @Query("SELECT * FROM world_people WHERE id = :id")
    suspend fun getById(id: String): WorldPersonEntity?

    @Query(
        """
        SELECT * FROM world_people
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<WorldPersonEntity>

    @Insert
    suspend fun insert(entity: WorldPersonEntity)

    @Update
    suspend fun update(entity: WorldPersonEntity)

    @Query("DELETE FROM world_people WHERE id = :id")
    suspend fun delete(id: String)
}

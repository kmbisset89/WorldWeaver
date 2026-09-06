package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldCelestialBodyDao {
    @Query("SELECT * FROM world_celestial_bodies WHERE worldId = :worldId ORDER BY sortIndex ASC, name ASC")
    fun observeByWorld(worldId: String): Flow<List<WorldCelestialBodyEntity>>

    @Query("SELECT * FROM world_celestial_bodies WHERE worldId = :worldId ORDER BY sortIndex ASC, name ASC")
    suspend fun getByWorld(worldId: String): List<WorldCelestialBodyEntity>

    @Query("SELECT * FROM world_celestial_bodies WHERE id = :id")
    suspend fun getById(id: String): WorldCelestialBodyEntity?

    @Query(
        """
        SELECT * FROM world_celestial_bodies
        WHERE name LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<WorldCelestialBodyEntity>

    @Insert
    suspend fun insert(entity: WorldCelestialBodyEntity)

    @Update
    suspend fun update(entity: WorldCelestialBodyEntity)

    @Query("DELETE FROM world_celestial_bodies WHERE id = :id")
    suspend fun delete(id: String)
}

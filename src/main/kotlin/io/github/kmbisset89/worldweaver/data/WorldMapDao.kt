package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldMapDao {
    @Query("SELECT * FROM world_maps WHERE worldId = :worldId ORDER BY locationId ASC")
    fun observeByWorld(worldId: String): Flow<List<WorldMapEntity>>

    @Query("SELECT * FROM world_maps WHERE worldId = :worldId ORDER BY locationId ASC")
    suspend fun getByWorld(worldId: String): List<WorldMapEntity>

    @Query("SELECT * FROM world_maps WHERE id = :id")
    suspend fun getById(id: String): WorldMapEntity?

    @Query("SELECT * FROM world_maps WHERE worldId = :worldId AND locationId IS NULL LIMIT 1")
    suspend fun getWorldRoot(worldId: String): WorldMapEntity?

    @Query("SELECT * FROM world_maps WHERE locationId = :locationId LIMIT 1")
    suspend fun getByLocationId(locationId: String): WorldMapEntity?

    @Insert
    suspend fun insert(entity: WorldMapEntity)

    @Update
    suspend fun update(entity: WorldMapEntity)

    @Query("DELETE FROM world_maps WHERE id = :id")
    suspend fun delete(id: String)
}

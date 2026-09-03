package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LocationDao {
    @Query("SELECT * FROM locations WHERE worldId = :worldId ORDER BY name ASC")
    fun observeByWorld(worldId: String): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    fun observeById(id: String): Flow<LocationEntity?>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getById(id: String): LocationEntity?

    @Query("SELECT * FROM locations WHERE worldId = :worldId ORDER BY name ASC")
    suspend fun getByWorld(worldId: String): List<LocationEntity>

    @Query("SELECT COUNT(*) FROM locations WHERE parentLocationId = :parentLocationId")
    suspend fun countByParent(parentLocationId: String): Int

    @Query(
        """
        SELECT * FROM locations
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR climate LIKE '%' || :query || '%'
           OR terrain LIKE '%' || :query || '%'
           OR government LIKE '%' || :query || '%'
           OR history LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
           OR landmarks LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<LocationEntity>

    @Insert
    suspend fun insert(entity: LocationEntity)

    @Update
    suspend fun update(entity: LocationEntity)

    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun delete(id: String)
}

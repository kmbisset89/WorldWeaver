package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AssetDao {
    @Query("SELECT * FROM assets WHERE worldId = :worldId ORDER BY updatedAtEpochMillis DESC")
    fun observeByWorld(worldId: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE worldId = :worldId ORDER BY updatedAtEpochMillis DESC")
    suspend fun getByWorld(worldId: String): List<AssetEntity>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getById(id: String): AssetEntity?

    @Query(
        """
        SELECT * FROM assets
        WHERE displayName LIKE '%' || :query || '%'
           OR originalFileName LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY updatedAtEpochMillis DESC
        """
    )
    suspend fun searchLike(query: String): List<AssetEntity>

    @Insert
    suspend fun insert(entity: AssetEntity)

    @Update
    suspend fun update(entity: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun delete(id: String)
}

package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RandomTableDao {
    @Query("SELECT * FROM random_tables WHERE worldId = :worldId ORDER BY name ASC")
    fun observeByWorld(worldId: String): Flow<List<RandomTableEntity>>

    @Query("SELECT * FROM random_tables WHERE worldId = :worldId ORDER BY name ASC")
    suspend fun getByWorld(worldId: String): List<RandomTableEntity>

    @Query("SELECT * FROM random_tables WHERE id = :id")
    suspend fun getById(id: String): RandomTableEntity?

    @Query(
        """
        SELECT * FROM random_tables
        WHERE name LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<RandomTableEntity>

    @Insert
    suspend fun insert(entity: RandomTableEntity)

    @Update
    suspend fun update(entity: RandomTableEntity)

    @Query("DELETE FROM random_tables WHERE id = :id")
    suspend fun delete(id: String)
}

package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorldDao {
    @Query("SELECT * FROM worlds ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<WorldEntity>>

    @Query("SELECT * FROM worlds WHERE id = :id")
    fun observeById(id: String): Flow<WorldEntity?>

    @Query("SELECT * FROM worlds WHERE id = :id")
    suspend fun getById(id: String): WorldEntity?

    @Insert
    suspend fun insert(entity: WorldEntity)

    @Update
    suspend fun update(entity: WorldEntity)

    @Query("DELETE FROM worlds WHERE id = :id")
    suspend fun delete(id: String)
}

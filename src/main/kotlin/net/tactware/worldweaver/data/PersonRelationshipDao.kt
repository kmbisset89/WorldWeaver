package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PersonRelationshipDao {
    @Query("SELECT * FROM person_relationships ORDER BY id ASC")
    fun observeAll(): Flow<List<PersonRelationshipEntity>>

    @Query("SELECT * FROM person_relationships WHERE id = :id")
    suspend fun getById(id: String): PersonRelationshipEntity?

    @Query("SELECT * FROM person_relationships ORDER BY id ASC")
    suspend fun getAll(): List<PersonRelationshipEntity>

    @Insert
    suspend fun insert(entity: PersonRelationshipEntity)

    @Query("DELETE FROM person_relationships WHERE id = :id")
    suspend fun delete(id: String)

    @Query(
        """
        DELETE FROM person_relationships
        WHERE (fromKind = :kind AND fromId = :personId)
           OR (toKind = :kind AND toId = :personId)
        """,
    )
    suspend fun deleteByPerson(kind: String, personId: String)
}

package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PersonCompanionDao {
    @Query("SELECT * FROM person_companions ORDER BY id ASC")
    fun observeAll(): Flow<List<PersonCompanionEntity>>

    @Query("SELECT * FROM person_companions WHERE id = :id")
    suspend fun getById(id: String): PersonCompanionEntity?

    @Query("SELECT * FROM person_companions ORDER BY id ASC")
    suspend fun getAll(): List<PersonCompanionEntity>

    @Query(
        """
        SELECT * FROM person_companions
        WHERE ownerKind = :ownerKind AND ownerId = :ownerId
          AND companionKind = :companionKind AND companionId = :companionId
        """,
    )
    suspend fun findByPair(
        ownerKind: String,
        ownerId: String,
        companionKind: String,
        companionId: String,
    ): PersonCompanionEntity?

    @Insert
    suspend fun insert(entity: PersonCompanionEntity)

    @Query("DELETE FROM person_companions WHERE id = :id")
    suspend fun delete(id: String)

    @Query(
        """
        DELETE FROM person_companions
        WHERE (ownerKind = :kind AND ownerId = :personId)
           OR (companionKind = :kind AND companionId = :personId)
        """,
    )
    suspend fun deleteByPerson(kind: String, personId: String)
}

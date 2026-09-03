package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FactionMembershipDao {
    @Query("SELECT * FROM faction_memberships ORDER BY id ASC")
    fun observeAll(): Flow<List<FactionMembershipEntity>>

    @Query("SELECT * FROM faction_memberships ORDER BY id ASC")
    suspend fun getAll(): List<FactionMembershipEntity>

    @Query("SELECT * FROM faction_memberships WHERE id = :id")
    suspend fun getById(id: String): FactionMembershipEntity?

    @Query("SELECT * FROM faction_memberships WHERE factionId = :factionId ORDER BY id ASC")
    suspend fun getByFaction(factionId: String): List<FactionMembershipEntity>

    @Query(
        """
        SELECT * FROM faction_memberships
        WHERE personKind = :kind AND personId = :personId
        ORDER BY id ASC
        """,
    )
    suspend fun getByPerson(kind: String, personId: String): List<FactionMembershipEntity>

    @Query("SELECT COUNT(*) FROM faction_memberships WHERE factionId = :factionId")
    suspend fun countByFaction(factionId: String): Int

    @Insert
    suspend fun insert(entity: FactionMembershipEntity)

    @Query("DELETE FROM faction_memberships WHERE id = :id")
    suspend fun delete(id: String)

    @Query(
        """
        DELETE FROM faction_memberships
        WHERE personKind = :kind AND personId = :personId
        """,
    )
    suspend fun deleteByPerson(kind: String, personId: String)

    @Query("DELETE FROM faction_memberships WHERE factionId = :factionId")
    suspend fun deleteByFaction(factionId: String)
}

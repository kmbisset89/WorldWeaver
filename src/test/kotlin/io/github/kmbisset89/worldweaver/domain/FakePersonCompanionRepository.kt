package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakePersonCompanionRepository : PersonCompanionRepository {
    private val companions = MutableStateFlow<List<PersonCompanion>>(emptyList())

    fun all(): List<PersonCompanion> = companions.value

    override fun observeAll(): Flow<List<PersonCompanion>> = companions

    override suspend fun getById(id: String): PersonCompanion? {
        return companions.value.firstOrNull { it.id == id }
    }

    override suspend fun getAll(): List<PersonCompanion> {
        return companions.value
    }

    override suspend fun findByPair(owner: PersonRef, companion: PersonRef): PersonCompanion? {
        return companions.value.firstOrNull { link ->
            sameRef(link.owner, owner) && sameRef(link.companion, companion)
        }
    }

    override suspend fun insert(companion: PersonCompanion) {
        companions.value = companions.value + companion
    }

    override suspend fun delete(id: String) {
        companions.value = companions.value.filterNot { it.id == id }
    }

    override suspend fun deleteByPerson(ref: PersonRef) {
        companions.value = companions.value.filterNot { link ->
            sameRef(link.owner, ref) || sameRef(link.companion, ref)
        }
    }

    private fun sameRef(left: PersonRef, right: PersonRef): Boolean {
        return left.id == right.id && left::class == right::class
    }
}

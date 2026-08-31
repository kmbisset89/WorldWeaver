package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeReferenceDocRepository : ReferenceDocRepository {
    private val docs = MutableStateFlow<List<ReferenceDoc>>(emptyList())

    fun all(): List<ReferenceDoc> = docs.value

    override fun observeByCampaign(campaignId: String): Flow<List<ReferenceDoc>> {
        return docs.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun getById(id: String): ReferenceDoc? {
        return docs.value.firstOrNull { it.id == id }
    }

    override suspend fun getByCampaign(campaignId: String): List<ReferenceDoc> {
        return docs.value.filter { it.campaignId == campaignId }
    }

    override suspend fun insert(doc: ReferenceDoc) {
        docs.value = docs.value + doc
    }

    override suspend fun update(doc: ReferenceDoc) {
        docs.value = docs.value.map { current ->
            if (current.id == doc.id) doc else current
        }
    }

    override suspend fun delete(id: String) {
        docs.value = docs.value.filterNot { it.id == id }
    }
}

package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.tactware.worldweaver.domain.Lore
import net.tactware.worldweaver.domain.LoreRepository

internal class LoreRepositoryImpl(
    private val loreDao: LoreDao,
    private val loreSecretDao: LoreSecretDao,
    private val loreHintDao: LoreHintDao,
    private val converter: LoreEntityConverter,
) : LoreRepository {
    override fun observeByWorld(worldId: String): Flow<List<Lore>> {
        return combine(
            loreDao.observeByWorld(worldId),
            loreSecretDao.observeByWorld(worldId),
            loreHintDao.observeByWorld(worldId),
        ) { loreEntities, secretEntities, hintEntities ->
            assemble(loreEntities, secretEntities, hintEntities)
        }
    }

    override suspend fun getById(id: String): Lore? {
        val entity = loreDao.getById(id) ?: return null
        val secrets = loreSecretDao.getByLore(id)
        val hints = secrets.flatMap { loreHintDao.getBySecret(it.id) }
        return converter.toLore(entity, converter.toSecrets(secrets, hints))
    }

    override suspend fun getByWorld(worldId: String): List<Lore> {
        return assemble(
            loreDao.getByWorld(worldId),
            loreSecretDao.getByWorld(worldId),
            loreHintDao.getByWorld(worldId),
        )
    }

    override suspend fun search(query: String): List<Lore> {
        return loreDao.searchLike(query).map { entity ->
            converter.toLore(entity, emptyList())
        }
    }

    override suspend fun insert(lore: Lore) {
        loreDao.insert(converter.toEntity(lore))
        replaceChildren(lore)
    }

    override suspend fun update(lore: Lore) {
        loreDao.update(converter.toEntity(lore))
        replaceChildren(lore)
    }

    override suspend fun delete(id: String) {
        loreDao.delete(id)
    }

    private suspend fun replaceChildren(lore: Lore) {
        loreHintDao.deleteByLore(lore.id)
        loreSecretDao.deleteByLore(lore.id)
        val secrets = converter.toSecretEntities(lore)
        if (secrets.isNotEmpty()) {
            loreSecretDao.insertAll(secrets)
        }
        val hints = converter.toHintEntities(lore)
        if (hints.isNotEmpty()) {
            loreHintDao.insertAll(hints)
        }
    }

    private fun assemble(
        loreEntities: List<LoreEntity>,
        secretEntities: List<LoreSecretEntity>,
        hintEntities: List<LoreHintEntity>,
    ): List<Lore> {
        val secretsByLore = secretEntities.groupBy { it.loreId }
        val hintsBySecret = hintEntities.groupBy { it.secretId }
        return loreEntities.map { entity ->
            val secrets = converter.toSecrets(
                secretsByLore[entity.id].orEmpty(),
                secretsByLore[entity.id].orEmpty().flatMap { secret ->
                    hintsBySecret[secret.id].orEmpty()
                },
            )
            converter.toLore(entity, secrets)
        }
    }
}

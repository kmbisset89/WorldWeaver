package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.LoreCategory
import io.github.kmbisset89.worldweaver.domain.LoreHint
import io.github.kmbisset89.worldweaver.domain.LoreSecret
import java.time.Instant

internal class LoreEntityConverter {
    fun toLore(
        entity: LoreEntity,
        secrets: List<LoreSecret>,
    ): Lore {
        return Lore(
            id = entity.id,
            worldId = entity.worldId,
            title = entity.title,
            content = entity.content,
            category = LoreCategory.fromStorage(entity.category),
            tags = decodeList(entity.tags),
            relatedEntryIds = decodeList(entity.relatedEntryIds),
            secrets = secrets,
            locationId = entity.locationId,
            characterId = entity.characterId,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(lore: Lore): LoreEntity {
        return LoreEntity(
            id = lore.id,
            worldId = lore.worldId,
            title = lore.title,
            content = lore.content,
            category = lore.category.name,
            tags = encodeList(lore.tags),
            relatedEntryIds = encodeList(lore.relatedEntryIds),
            locationId = lore.locationId,
            characterId = lore.characterId,
            createdAtEpochMillis = lore.createdAt.toEpochMilli(),
            updatedAtEpochMillis = lore.updatedAt.toEpochMilli(),
        )
    }

    fun toSecretEntities(lore: Lore): List<LoreSecretEntity> {
        return lore.secrets.mapIndexed { index, secret ->
            LoreSecretEntity(
                id = secret.id,
                loreId = lore.id,
                title = secret.title,
                secret = secret.secret,
                sortIndex = index,
            )
        }
    }

    fun toHintEntities(lore: Lore): List<LoreHintEntity> {
        return lore.secrets.flatMap { secret ->
            secret.hints.mapIndexed { index, hint ->
                LoreHintEntity(
                    id = hint.id,
                    secretId = secret.id,
                    text = hint.text,
                    revealed = hint.revealed,
                    sortIndex = index,
                )
            }
        }
    }

    fun toSecrets(
        secretEntities: List<LoreSecretEntity>,
        hintEntities: List<LoreHintEntity>,
    ): List<LoreSecret> {
        val hintsBySecret = hintEntities.groupBy { it.secretId }
        return secretEntities.map { entity ->
            LoreSecret(
                id = entity.id,
                title = entity.title,
                secret = entity.secret,
                hints = hintsBySecret[entity.id].orEmpty().map { hint ->
                    LoreHint(
                        id = hint.id,
                        text = hint.text,
                        revealed = hint.revealed,
                    )
                },
            )
        }
    }

    private fun encodeList(values: List<String>): String {
        return values.joinToString(LIST_SEPARATOR)
    }

    private fun decodeList(value: String): List<String> {
        if (value.isEmpty()) {
            return emptyList()
        }
        return value.split(LIST_SEPARATOR).filter { it.isNotEmpty() }
    }

    private companion object {
        const val LIST_SEPARATOR = "\n"
    }
}

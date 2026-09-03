package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal class ObservePersonCompanionsUseCase(
    private val personCompanionRepository: PersonCompanionRepository,
) {
    operator fun invoke(): Flow<List<PersonCompanion>> {
        return personCompanionRepository.observeAll()
    }
}

package net.tactware.worldweaver.domain

internal class CreatePersonCompanionUseCase(
    private val personCompanionRepository: PersonCompanionRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val entityIdFactory: EntityIdFactory,
) {
    sealed interface Result {
        data class Created(val companion: PersonCompanion) : Result
        data object InvalidTarget : Result
        data object SelfCompanion : Result
        data object InvalidCompanionKind : Result
        data object AlreadyLinked : Result
    }

    suspend operator fun invoke(
        owner: PersonRef,
        companion: PersonRef,
        kind: CompanionKind,
    ): Result {
        if (owner.id == companion.id && owner::class == companion::class) {
            return Result.SelfCompanion
        }
        if (personKind(owner) == null || personKind(companion) == null) {
            return Result.InvalidTarget
        }
        val companionKind = personKind(companion) ?: return Result.InvalidTarget
        if (companionKind == PersonKind.PlayerCharacter) {
            return Result.InvalidCompanionKind
        }
        if (personCompanionRepository.findByPair(owner, companion) != null) {
            return Result.AlreadyLinked
        }
        val link = PersonCompanion(
            id = entityIdFactory.create(),
            owner = owner,
            companion = companion,
            kind = kind,
        )
        personCompanionRepository.insert(link)
        return Result.Created(link)
    }

    private suspend fun personKind(ref: PersonRef): PersonKind? {
        return when (ref) {
            is PersonRef.World -> worldPersonRepository.getById(ref.id)?.kind
            is PersonRef.Campaign -> campaignPersonRepository.getById(ref.id)?.kind
        }
    }
}

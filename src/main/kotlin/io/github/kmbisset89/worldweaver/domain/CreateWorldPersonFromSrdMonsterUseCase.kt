package io.github.kmbisset89.worldweaver.domain

internal class CreateWorldPersonFromSrdMonsterUseCase(
    private val createWorldPerson: CreateWorldPersonUseCase,
    private val catalogRepository: SrdCatalogRepository,
    private val resolver: FifthEditionPickerCatalogResolver,
) {
    sealed interface Result {
        data class Created(val person: WorldPerson) : Result
        data object NotFound : Result
        data object NoActiveWorld : Result
        data object InvalidName : Result
    }

    suspend operator fun invoke(monsterName: String): Result {
        val catalog = resolver.resolve(catalogRepository.get())
        val monster = catalog.monsterNamed(monsterName.trim()) ?: return Result.NotFound
        val created = createWorldPerson(
            WorldPersonDraft(
                kind = PersonKind.Monster,
                name = monster.name,
                description = monsterDescription(monster),
                sheet = FifthEditionSheet.empty().copy(
                    hitPoints = monster.hitPoints,
                    maxHitPoints = monster.hitPoints,
                    armorClass = monster.armorClass,
                    walkSpeed = monster.walkSpeed,
                    notes = monsterNotes(monster),
                ),
            ),
        )
        return when (created) {
            is CreateWorldPersonUseCase.Result.Created -> Result.Created(created.person)
            CreateWorldPersonUseCase.Result.NoActiveWorld -> Result.NoActiveWorld
            CreateWorldPersonUseCase.Result.InvalidName -> Result.InvalidName
            CreateWorldPersonUseCase.Result.InvalidKind -> Result.InvalidName
        }
    }

    private fun monsterDescription(monster: SrdMonsterEntry): String {
        val type = monster.creatureType.takeIf { it.isNotBlank() }
        val cr = monster.challengeRating.takeIf { it.isNotBlank() }?.let { "CR $it" }
        return listOfNotNull(type, cr).joinToString(" · ")
    }

    private fun monsterNotes(monster: SrdMonsterEntry): String {
        val cr = monster.challengeRating.takeIf { it.isNotBlank() }?.let { "CR $it." }
        val type = monster.creatureType.takeIf { it.isNotBlank() }?.let { "$it." }
        return listOfNotNull(cr, type).joinToString(" ")
    }
}

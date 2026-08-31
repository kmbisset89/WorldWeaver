package net.tactware.worldweaver.domain

internal class GenerateRandomNpcUseCase(
    private val abilityScoreRoller: AbilityScoreRoller,
    private val nextNameIndex: () -> Int = {
        kotlin.random.Random.nextInt(FifthEditionReference.npcNames.size)
    },
    private val nextRaceIndex: () -> Int = {
        kotlin.random.Random.nextInt(FifthEditionReference.generatorRaces.size)
    },
) {
    operator fun invoke(method: AbilityScoreMethod): RandomNpcDraft {
        val names = FifthEditionReference.npcNames
        val races = FifthEditionReference.generatorRaces
        return RandomNpcDraft(
            name = names[nextNameIndex().mod(names.size)],
            race = races[nextRaceIndex().mod(races.size)],
            abilityScores = abilityScoreRoller.roll(method),
        )
    }
}

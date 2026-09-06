package io.github.kmbisset89.worldweaver.domain

internal class CreateAtmosphereMoodUseCase(
    private val store: AtmosphereSettingsStore,
    private val entityIdFactory: EntityIdFactory,
    private val colorParser: GoveeColorHexParser = GoveeColorHexParser(),
) {
    sealed interface Result {
        data class Created(val mood: AtmosphereMood) : Result
        data object InvalidName : Result
        data object InvalidColor : Result
        data object DuplicateName : Result
    }

    operator fun invoke(
        name: String,
        powerOn: Boolean,
        brightness: Int,
        colorHex: String,
    ): Result {
        val trimmedName = name.trim()
        val trimmedColor = colorHex.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        if (colorParser.parse(trimmedColor) == null) {
            return Result.InvalidColor
        }
        val existing = store.settings.value.moods
        val duplicateCustom = existing.any { it.name.equals(trimmedName, ignoreCase = true) }
        val duplicateBuiltIn = GoveeLightingPreset.entries.any {
            it.displayName.equals(trimmedName, ignoreCase = true)
        }
        if (duplicateCustom || duplicateBuiltIn) {
            return Result.DuplicateName
        }
        val mood = AtmosphereMood(
            id = entityIdFactory.create(),
            name = trimmedName,
            powerOn = powerOn,
            brightness = brightness.coerceIn(1, 100),
            colorHex = trimmedColor,
            sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1,
        )
        store.setMoods(existing + mood)
        return Result.Created(mood)
    }
}

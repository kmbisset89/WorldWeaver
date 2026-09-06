package io.github.kmbisset89.worldweaver.domain

internal class CreateAtmosphereSceneUseCase(
    private val store: AtmosphereSettingsStore,
    private val entityIdFactory: EntityIdFactory,
    private val colorParser: GoveeColorHexParser = GoveeColorHexParser(),
) {
    sealed interface Result {
        data class Created(val scene: AtmosphereScene) : Result
        data object InvalidName : Result
        data object InvalidEntityId : Result
        data object InvalidGoveeColor : Result
        data object NoTargets : Result
        data object DuplicateName : Result
        data object DuplicateEntityId : Result
    }

    operator fun invoke(
        name: String,
        entityId: String = "",
        hueSceneId: String = "",
        hueGroupId: String = "",
        hueLightIds: List<String> = emptyList(),
        goveeDeviceIds: List<String> = emptyList(),
        goveePowerOn: Boolean = true,
        goveeBrightness: Int = 80,
        goveeColorHex: String = "",
    ): Result {
        val trimmedName = name.trim()
        val trimmedEntityId = entityId.trim()
        val trimmedHueSceneId = hueSceneId.trim()
        val trimmedHueGroupId = hueGroupId.trim()
        val lights = hueLightIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val trimmedColor = goveeColorHex.trim()
        val devices = goveeDeviceIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        if (trimmedEntityId.isNotEmpty() && !SCENE_ENTITY_ID.matches(trimmedEntityId)) {
            return Result.InvalidEntityId
        }
        val usesLook = devices.isNotEmpty() || (lights.isNotEmpty() && trimmedHueSceneId.isEmpty())
        if (usesLook && goveePowerOn && colorParser.parse(trimmedColor) == null) {
            return Result.InvalidGoveeColor
        }
        val hasTargets = trimmedEntityId.isNotEmpty() ||
            trimmedHueSceneId.isNotEmpty() ||
            lights.isNotEmpty() ||
            devices.isNotEmpty()
        if (!hasTargets) {
            return Result.NoTargets
        }
        val existing = store.settings.value.scenes
        if (existing.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            return Result.DuplicateName
        }
        if (trimmedEntityId.isNotEmpty() && existing.any { it.entityId == trimmedEntityId }) {
            return Result.DuplicateEntityId
        }
        val scene = AtmosphereScene(
            id = entityIdFactory.create(),
            name = trimmedName,
            entityId = trimmedEntityId,
            sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1,
            hueSceneId = trimmedHueSceneId,
            hueGroupId = trimmedHueGroupId,
            hueLightIds = lights,
            goveeDeviceIds = devices,
            goveePowerOn = goveePowerOn,
            goveeBrightness = goveeBrightness.coerceIn(1, 100),
            goveeColorHex = trimmedColor,
        )
        store.setScenes(existing + scene)
        return Result.Created(scene)
    }

    private companion object {
        val SCENE_ENTITY_ID = Regex("^scene\\.[a-z0-9_]+$")
    }
}

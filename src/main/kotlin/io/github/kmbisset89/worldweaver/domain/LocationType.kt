package io.github.kmbisset89.worldweaver.domain

internal enum class LocationType(
    val displayName: String,
) {
    Continent("Continent"),
    Area("Area"),
    City("City"),
    Place("Place"),
    ;

    fun requiredParentType(): LocationType? {
        return when (this) {
            Continent -> null
            Area -> Continent
            City -> Area
            Place -> City
        }
    }

    fun acceptsParent(parent: Location?): Boolean {
        val required = requiredParentType()
        return if (required == null) {
            parent == null
        } else {
            parent?.type == required
        }
    }

    companion object {
        fun fromStorage(value: String): LocationType {
            return entries.firstOrNull { it.name == value } ?: Place
        }
    }
}

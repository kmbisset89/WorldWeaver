package net.tactware.worldweaver.domain

internal data class GridDistance(
    val squares: Int,
    val units: Double,
    val path: List<GridCell>,
) {
    fun unitsLabel(): String {
        return if (units == units.toLong().toDouble()) {
            units.toLong().toString()
        } else {
            units.toString()
        }
    }
}

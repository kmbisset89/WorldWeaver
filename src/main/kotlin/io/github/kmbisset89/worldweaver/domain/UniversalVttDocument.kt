package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A Universal VTT map file (`.uvtt` / `.dd2vtt`): embedded PNG plus grid metadata.
 *
 * Walls, portals, and lights are empty. World Weaver does not author Foundry line of sight.
 */
@Serializable
internal data class UniversalVttDocument(
    val format: Double,
    val resolution: Resolution,
    @SerialName("line_of_sight")
    val lineOfSight: List<List<Point>>,
    @SerialName("objects_line_of_sight")
    val objectsLineOfSight: List<List<Point>>,
    val portals: List<JsonElement>,
    val environment: Environment,
    val lights: List<JsonElement>,
    val image: String,
) {
    @Serializable
    data class Resolution(
        @SerialName("map_origin")
        val mapOrigin: Point,
        @SerialName("map_size")
        val mapSize: Point,
        @SerialName("pixels_per_grid")
        val pixelsPerGrid: Int,
    )

    @Serializable
    data class Point(
        val x: Double,
        val y: Double,
    )

    @Serializable
    data class Environment(
        @SerialName("baked_lighting")
        val bakedLighting: Boolean,
        @SerialName("ambient_light")
        val ambientLight: String,
    )
}

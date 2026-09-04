package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class WorldMapFileStore(
    private val mapsRoot: File,
) {
    fun write(worldMapId: String, pyramid: MapTilePyramid) {
        val mapDir = mapDirectory(worldMapId)
        if (mapDir.exists()) {
            mapDir.deleteRecursively()
        }
        mapDir.mkdirs()
        File(mapDir, ORIGINAL_FILE_NAME).writeBytes(pyramid.originalPng)
        pyramid.tiles.forEach { tile ->
            val tileFile = tileFile(worldMapId, tile.zoom, tile.x, tile.y)
            tileFile.parentFile.mkdirs()
            tileFile.writeBytes(tile.imagePng)
        }
    }

    fun readTile(worldMapId: String, zoom: Int, x: Int, y: Int): ByteArray? {
        return readBytesIfPresent(tileFile(worldMapId, zoom, x, y))
    }

    fun delete(worldMapId: String) {
        val mapDir = mapDirectory(worldMapId)
        if (mapDir.exists()) {
            mapDir.deleteRecursively()
        }
    }

    fun listRelativeFiles(worldMapId: String): List<Pair<String, ByteArray>> {
        val mapDir = mapDirectory(worldMapId)
        if (!mapDir.isDirectory) {
            return emptyList()
        }
        return mapDir.walkTopDown()
            .filter { it.isFile }
            .map { file ->
                file.relativeTo(mapDir).invariantSeparatorsPath to file.readBytes()
            }
            .toList()
    }

    fun writeRelativeFiles(worldMapId: String, files: List<Pair<String, ByteArray>>) {
        val mapDir = mapDirectory(worldMapId)
        if (mapDir.exists()) {
            mapDir.deleteRecursively()
        }
        files.forEach { (relativePath, bytes) ->
            val dest = File(mapDir, relativePath)
            dest.parentFile.mkdirs()
            dest.writeBytes(bytes)
        }
    }

    private fun mapDirectory(worldMapId: String): File {
        return File(mapsRoot, worldMapId)
    }

    private fun tileFile(worldMapId: String, zoom: Int, x: Int, y: Int): File {
        return File(File(mapDirectory(worldMapId), "tiles/$zoom"), "${x}_$y.png")
    }

    private fun readBytesIfPresent(file: File): ByteArray? {
        if (!file.isFile) {
            return null
        }
        return file.readBytes()
    }

    private companion object {
        const val ORIGINAL_FILE_NAME = "original.png"
    }
}

package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class BattleMapFileStore(
    private val mapsRoot: File,
) {
    fun write(battleMapId: String, pyramid: MapTilePyramid) {
        val mapDir = mapDirectory(battleMapId)
        if (mapDir.exists()) {
            mapDir.deleteRecursively()
        }
        mapDir.mkdirs()
        File(mapDir, ORIGINAL_FILE_NAME).writeBytes(pyramid.originalPng)
        pyramid.tiles.forEach { tile ->
            val tileFile = tileFile(battleMapId, tile.zoom, tile.x, tile.y)
            tileFile.parentFile.mkdirs()
            tileFile.writeBytes(tile.imagePng)
        }
    }

    fun readOriginalPng(battleMapId: String): ByteArray? {
        return readBytesIfPresent(File(mapDirectory(battleMapId), ORIGINAL_FILE_NAME))
    }

    fun readTile(battleMapId: String, zoom: Int, x: Int, y: Int): ByteArray? {
        return readBytesIfPresent(tileFile(battleMapId, zoom, x, y))
    }

    fun writeSituation(battleMapId: String, situationId: String, pyramid: MapTilePyramid) {
        val situationDir = situationDirectory(battleMapId, situationId)
        if (situationDir.exists()) {
            situationDir.deleteRecursively()
        }
        situationDir.mkdirs()
        File(situationDir, ORIGINAL_FILE_NAME).writeBytes(pyramid.originalPng)
        pyramid.tiles.forEach { tile ->
            val tileFile = situationTileFile(battleMapId, situationId, tile.zoom, tile.x, tile.y)
            tileFile.parentFile.mkdirs()
            tileFile.writeBytes(tile.imagePng)
        }
    }

    fun readSituationTile(
        battleMapId: String,
        situationId: String,
        zoom: Int,
        x: Int,
        y: Int,
    ): ByteArray? {
        return readBytesIfPresent(situationTileFile(battleMapId, situationId, zoom, x, y))
    }

    fun deleteSituation(battleMapId: String, situationId: String) {
        val situationDir = situationDirectory(battleMapId, situationId)
        if (situationDir.exists()) {
            situationDir.deleteRecursively()
        }
    }

    fun delete(battleMapId: String) {
        val mapDir = mapDirectory(battleMapId)
        if (mapDir.exists()) {
            mapDir.deleteRecursively()
        }
    }

    fun listRelativeFiles(battleMapId: String): List<Pair<String, ByteArray>> {
        val mapDir = mapDirectory(battleMapId)
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

    fun writeRelativeFiles(battleMapId: String, files: List<Pair<String, ByteArray>>) {
        val mapDir = mapDirectory(battleMapId)
        if (mapDir.exists()) {
            mapDir.deleteRecursively()
        }
        files.forEach { (relativePath, bytes) ->
            val dest = File(mapDir, relativePath)
            dest.parentFile.mkdirs()
            dest.writeBytes(bytes)
        }
    }

    private fun mapDirectory(battleMapId: String): File {
        return File(mapsRoot, battleMapId)
    }

    private fun situationDirectory(battleMapId: String, situationId: String): File {
        return File(File(mapDirectory(battleMapId), SITUATIONS_DIR_NAME), situationId)
    }

    private fun tileFile(battleMapId: String, zoom: Int, x: Int, y: Int): File {
        return File(File(mapDirectory(battleMapId), "tiles/$zoom"), "${x}_$y.png")
    }

    private fun situationTileFile(
        battleMapId: String,
        situationId: String,
        zoom: Int,
        x: Int,
        y: Int,
    ): File {
        return File(File(situationDirectory(battleMapId, situationId), "tiles/$zoom"), "${x}_$y.png")
    }

    private fun readBytesIfPresent(file: File): ByteArray? {
        if (!file.isFile) {
            return null
        }
        return file.readBytes()
    }

    private companion object {
        const val ORIGINAL_FILE_NAME = "original.png"
        const val SITUATIONS_DIR_NAME = "situations"
    }
}

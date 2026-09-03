package net.tactware.worldweaver.domain

import java.io.File

/**
 * Reads starter battle map PNGs from `battle_maps` and `medium_battle_maps` beside the app.
 */
internal class BundledBattleMapCatalogLoader(
    private val roots: List<File> = defaultRoots(),
) {
    fun isAvailable(): Boolean {
        return resolvedRoot() != null
    }

    fun loadPng(fileName: String): ByteArray? {
        val file = roots.firstNotNullOfOrNull { root ->
            File(root, fileName).takeIf { it.isFile }
        } ?: return null
        return file.readBytes()
    }

    fun resolvedRoot(): File? {
        return roots.firstOrNull { root ->
            root.isDirectory && BundledBattleMapCatalog.entries.any { entry ->
                File(root, entry.fileName).isFile
            }
        }
    }

    companion object {
        const val SMALL_FOLDER_NAME = "battle_maps"
        const val MEDIUM_FOLDER_NAME = "medium_battle_maps"
        const val COMPOSE_RESOURCES_DIR = "compose.application.resources.dir"

        fun defaultRoots(): List<File> {
            return buildList {
                val parents = buildList {
                    val composeDir = System.getProperty(COMPOSE_RESOURCES_DIR)
                    if (!composeDir.isNullOrBlank()) {
                        add(File(composeDir))
                    }
                    add(File("."))
                    val userDir = System.getProperty("user.dir")
                    if (!userDir.isNullOrBlank()) {
                        add(File(userDir))
                    }
                }
                for (parent in parents.distinct()) {
                    add(File(parent, SMALL_FOLDER_NAME))
                    add(File(parent, MEDIUM_FOLDER_NAME))
                }
            }
        }
    }
}

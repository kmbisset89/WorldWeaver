package net.tactware.worldweaver.domain

internal class BundledSrdCatalogLoader(
    private val converter: SrdCatalogJsonConverter,
) {
    fun load(): SrdCatalogPayload {
        val stream = javaClass.classLoader.getResourceAsStream(RESOURCE_NAME)
            ?: error("Bundled SRD catalog is missing")
        return stream.bufferedReader().use { reader ->
            converter.decode(reader.readText())
        }
    }

    companion object {
        const val RESOURCE_NAME = "srd5e.json"
    }
}

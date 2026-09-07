package io.github.kmbisset89.worldweaver.domain

internal class WikilinkTextResolver(
    private val parser: WikilinkTextParser = WikilinkTextParser(),
) {
    fun resolve(text: String, catalog: WikilinkCatalog): List<WikilinkDisplaySpan> {
        if (text.isEmpty()) {
            return emptyList()
        }
        return parser.parse(text).map { segment ->
            when (segment) {
                is WikilinkSegment.Text -> WikilinkDisplaySpan(
                    text = segment.value,
                    target = null,
                )
                is WikilinkSegment.Link -> {
                    val target = catalog.resolve(segment)
                    if (target == null) {
                        WikilinkDisplaySpan(
                            text = segment.raw,
                            target = null,
                            unresolved = true,
                        )
                    } else {
                        WikilinkDisplaySpan(
                            text = segment.alias?.ifBlank { target.title } ?: target.title,
                            target = target,
                        )
                    }
                }
            }
        }
    }
}

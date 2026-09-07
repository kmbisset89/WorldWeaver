package io.github.kmbisset89.worldweaver.domain

internal class WikilinkDraftCompleter {
    fun complete(text: String, target: WikilinkTarget): String {
        val lastOpen = text.lastIndexOf("[[")
        if (lastOpen < 0) {
            return text + target.insertionToken()
        }
        val after = text.substring(lastOpen + 2)
        if (after.contains("]]") || after.contains('\n')) {
            return text + target.insertionToken()
        }
        return text.take(lastOpen) + target.insertionToken()
    }
}

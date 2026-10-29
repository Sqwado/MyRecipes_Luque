package com.example.myrecipes_luque.util

object InstructionParser {

    private val numberedPrefix = Regex("^\\d+[.)]\\s*")
    private val stepLabelOnly = Regex(
        """^(?:step|étape|etape)\s*\d+\s*[:.\-]*$""",
        RegexOption.IGNORE_CASE,
    )
    private val stepLabelPrefix = Regex(
        """^(?:step|étape|etape)\s*\d+\s*[:.\-]+\s*""",
        RegexOption.IGNORE_CASE,
    )
    private val sentenceSplit = Regex("(?<=[.!?])\\s+(?=\\p{Lu})")

    /** Découpe le texte TheMealDB en étapes lisibles (ignore les titres "step N"). */
    fun parse(raw: String?): List<String> {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return emptyList()

        val byLines = text
            .split(Regex("\\r?\\n+"))
            .map { normalizeLine(it) }
            .filter { it.isNotEmpty() }

        if (byLines.size > 1) return byLines

        val bySentences = text
            .split(sentenceSplit)
            .map { normalizeLine(it) }
            .filter { it.isNotEmpty() }

        return bySentences.ifEmpty {
            listOfNotNull(normalizeLine(text).takeIf { it.isNotEmpty() })
        }
    }

    private fun normalizeLine(line: String): String {
        var value = line.trim()
        if (value.isEmpty()) return ""
        if (stepLabelOnly.matches(value)) return ""

        value = value.replace(numberedPrefix, "")
        value = value.replace(stepLabelPrefix, "")
        return value.trim()
    }
}

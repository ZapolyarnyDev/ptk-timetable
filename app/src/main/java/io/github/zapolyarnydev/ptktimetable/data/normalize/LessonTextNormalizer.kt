package io.github.zapolyarnydev.ptktimetable.data.normalize

import java.util.Locale

data class NormalizedLessonText(
    val subject: String,
    val teacher: String?,
    val classroom: String?
)

class LessonTextNormalizer {

    fun normalize(rawText: String): NormalizedLessonText {
        val clean = normalizeSpaces(rawText)
        if (clean.isBlank()) return NormalizedLessonText("", null, null)

        val parts = clean
            .split(',')
            .map { normalizeSpaces(it) }
            .filter { hasMeaningfulText(it) }

        if (parts.isEmpty()) return NormalizedLessonText(clean, null, null)

        if (parts.size >= 3) {
            val subject = normalizeSpaces(parts.dropLast(2).joinToString(", "))
                .ifBlank { parts.first() }
            val teacher = parts[parts.size - 2].takeIf { hasMeaningfulText(it) }
            val classroom = parts.last().takeIf { hasMeaningfulText(it) }
            return NormalizedLessonText(subject, teacher, classroom)
        }

        if (parts.size == 2) {
            val second = parts[1]
            return if (looksLikeClassroom(second)) {
                NormalizedLessonText(parts[0], null, second)
            } else {
                NormalizedLessonText(parts[0], second, null)
            }
        }

        return NormalizedLessonText(parts[0], null, null)
    }

    private fun looksLikeClassroom(text: String): Boolean {
        val normalized = text.lowercase(Locale.ROOT).replace('ё', 'е')
        if (normalized.isBlank()) return false
        if (ROOM_KEYWORDS.any { normalized.contains(it) }) return true
        return DIGIT_REGEX.containsMatchIn(normalized)
    }

    private fun hasMeaningfulText(text: String): Boolean {
        val normalized = normalizeSpaces(text)
        return normalized.isNotBlank() && !DASH_ONLY_REGEX.matches(normalized)
    }

    private fun normalizeSpaces(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim()
    }

    private companion object {
        val DASH_ONLY_REGEX = Regex("^[-—–]+$")
        val DIGIT_REGEX = Regex("\\d+")
        val ROOM_KEYWORDS = listOf(
            "ауд",
            "каб",
            "зал",
            "новгу",
            "школа",
            "нтш",
            "aud",
            "room"
        )
    }
}

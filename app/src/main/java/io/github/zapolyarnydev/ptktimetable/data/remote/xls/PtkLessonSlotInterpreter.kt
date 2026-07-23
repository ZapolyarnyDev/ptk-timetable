package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.Sheet

internal class PtkLessonSlotInterpreter(private val reader: XlsSheetReader) {

    fun read(sheet: Sheet, lessonColumn: Int, rowIndex: Int, nextTimeRow: Int): List<Pair<String, WeekType>> {
        val rawSegments = buildList<RowSegment> {
            for (lessonRow in rowIndex until nextTimeRow) {
                val rawText = reader.text(sheet, lessonRow, lessonColumn)
                val text = normalize(rawText)
                if (text.isBlank() || lastOrNull()?.text == text) continue
                add(RowSegment(lessonRow, text, rawText))
            }
        }
        val segments = rawSegments.filterNot { isHeaderNoise(it.text) }
        if (segments.isEmpty()) return emptyList()

        if (isSplitSlot(sheet, lessonColumn, rowIndex, nextTimeRow)) {
            return if (segments.size >= 2) {
                mapSplitRows(segments[0].text, segments[1].text)
            } else {
                mapSingleSplitSegment(sheet, lessonColumn, rowIndex, nextTimeRow, segments.first())
            }
        }

        if (segments.size >= 2) return mapSplitRows(segments[0].text, segments[1].text)
        val segment = segments.first()
        return mapSingleCell(
            segment = segment,
            preferLower = hasDashedDividerBeforeRow(sheet, lessonColumn, rowIndex, segment.rowIndex),
        )
    }

    fun hasLessonText(value: String): Boolean {
        val normalized = normalize(value)
        return normalized.isNotBlank() && !DASH_ONLY_REGEX.matches(normalized)
    }

    fun isHeaderNoise(value: String): Boolean {
        val normalized = value.lowercase()
            .replace('ё', 'е')
            .replace(".", "")
            .replace(",", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return normalized.contains("занятия") ||
            normalized.contains("день недели") ||
            normalized.contains("зам директора")
    }

    private fun mapSingleSplitSegment(
        sheet: Sheet,
        lessonColumn: Int,
        rowIndex: Int,
        nextTimeRow: Int,
        segment: RowSegment,
    ): List<Pair<String, WeekType>> {
        val hasDivider = hasDashedDividerBeforeRow(sheet, lessonColumn, rowIndex, segment.rowIndex) ||
            hasDashedBottomAtRow(sheet, lessonColumn, segment.rowIndex)
        if (hasDivider) return listOf(segment.text to WeekType.LOWER)
        val splitPoint = rowIndex + ((nextTimeRow - rowIndex) / 2)
        val weekType = if (segment.rowIndex >= splitPoint) WeekType.LOWER else WeekType.ALL
        return listOf(segment.text to weekType)
    }

    private fun mapSplitRows(topText: String, bottomText: String): List<Pair<String, WeekType>> {
        val topHasLesson = hasLessonText(topText)
        val bottomHasLesson = hasLessonText(bottomText)
        return when {
            topHasLesson && bottomHasLesson -> listOf(
                topText to WeekType.UPPER,
                bottomText to WeekType.LOWER,
            )

            topHasLesson -> listOf(topText to WeekType.UPPER)

            bottomHasLesson -> listOf(bottomText to WeekType.LOWER)

            else -> emptyList()
        }
    }

    private fun mapSingleCell(segment: RowSegment, preferLower: Boolean): List<Pair<String, WeekType>> {
        if (!hasLessonText(segment.text)) return emptyList()
        val weekType = if (looksLikeLowerOnlyCell(segment.rawText) || preferLower) {
            WeekType.LOWER
        } else {
            WeekType.ALL
        }
        return listOf(segment.text to weekType)
    }

    private fun looksLikeLowerOnlyCell(rawText: String): Boolean {
        val lines = rawText.replace("\r", "").split('\n')
        if (lines.size < 2) return false
        return lines.indexOfFirst { normalize(it).isNotBlank() && !DASH_ONLY_REGEX.matches(normalize(it)) } > 0
    }

    private fun hasDashedDividerBeforeRow(
        sheet: Sheet,
        lessonColumn: Int,
        fromRow: Int,
        toRowExclusive: Int,
    ): Boolean {
        if (fromRow >= toRowExclusive) return false
        for (candidateRow in fromRow until toRowExclusive) {
            val border = sheet.getRow(candidateRow)
                ?.getCell(lessonColumn)
                ?.cellStyle
                ?.borderBottom
                ?: continue
            if (isDashedBorder(border)) return true
        }
        return false
    }

    private fun hasDashedBottomAtRow(sheet: Sheet, lessonColumn: Int, rowIndex: Int): Boolean {
        val border = sheet.getRow(rowIndex)
            ?.getCell(lessonColumn)
            ?.cellStyle
            ?.borderBottom
            ?: return false
        return isDashedBorder(border)
    }

    private fun isSplitSlot(sheet: Sheet, lessonColumn: Int, rowIndex: Int, nextTimeRow: Int): Boolean {
        if (nextTimeRow - rowIndex < 2) return false
        val merged = reader.mergedRegion(sheet, rowIndex, lessonColumn)
        return merged == null || merged.firstRow > rowIndex || merged.lastRow < nextTimeRow - 1
    }

    private fun isDashedBorder(borderStyle: BorderStyle): Boolean = borderStyle == BorderStyle.DASHED ||
        borderStyle == BorderStyle.DOTTED ||
        borderStyle == BorderStyle.MEDIUM_DASHED ||
        borderStyle == BorderStyle.MEDIUM_DASH_DOT ||
        borderStyle == BorderStyle.MEDIUM_DASH_DOT_DOT ||
        borderStyle == BorderStyle.SLANTED_DASH_DOT

    private fun normalize(value: String): String = value.replace(Regex("\\s+"), " ").trim()

    private data class RowSegment(val rowIndex: Int, val text: String, val rawText: String)

    private companion object {
        val DASH_ONLY_REGEX = Regex("^[-—–]+$")
    }
}

package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import org.apache.poi.ss.usermodel.Sheet
import java.util.Locale

internal data class GroupLayout(val dayColumn: Int, val timeColumn: Int, val lessonColumn: Int)

internal class PtkSheetLayoutDetector(private val reader: XlsSheetReader) {

    fun find(sheet: Sheet, normalizedGroupName: String): GroupLayout? {
        val maxColumn = reader.maxColumn(sheet)
        val maxHeaderRow = minOf(sheet.lastRowNum, HEADER_SCAN_MAX_ROW)
        val candidates = mutableListOf<BlockCandidate>()
        var blockStart = 0

        while (blockStart + 2 <= maxColumn) {
            val layout = GroupLayout(blockStart, blockStart + 1, blockStart + 2)
            for (rowIndex in 0..maxHeaderRow) {
                for (columnIndex in blockStart..layout.lessonColumn) {
                    val text = normalize(reader.text(sheet, rowIndex, columnIndex))
                    if (!containsGroupToken(text, normalizedGroupName)) continue
                    candidates += BlockCandidate(
                        layout = layout,
                        rowIndex = rowIndex,
                        exact = text == normalizedGroupName,
                        tokenCount = tokenize(text).size,
                    )
                }
            }
            blockStart += 3
        }

        return candidates
            .sortedWith(
                compareByDescending<BlockCandidate> { it.exact }
                    .thenBy { it.tokenCount }
                    .thenBy { it.rowIndex },
            )
            .firstOrNull()
            ?.layout
    }

    fun timeRows(sheet: Sheet, timeColumn: Int): List<Int> = buildList {
        for (rowIndex in 0..sheet.lastRowNum) {
            val merged = reader.mergedRegion(sheet, rowIndex, timeColumn)
            if (merged != null && merged.firstRow < rowIndex) continue
            if (isTimeRange(reader.text(sheet, rowIndex, timeColumn))) add(rowIndex)
        }
    }

    private fun containsGroupToken(cellText: String, normalizedGroupName: String): Boolean {
        if (normalize(cellText) == normalizedGroupName) return true
        return tokenize(cellText).any { it == normalizedGroupName }
    }

    private fun tokenize(text: String): List<String> = text.lowercase(Locale.ROOT)
        .split(Regex("[^\\p{L}\\p{Nd}]+"))
        .filter { it.isNotBlank() }

    private fun isTimeRange(value: String): Boolean {
        if (value.isBlank()) return false
        return TIME_RANGE_REGEX.containsMatchIn(value.replace('—', '-').replace('–', '-'))
    }

    private fun normalize(value: String): String = value.replace(Regex("\\s+"), " ").trim()

    private data class BlockCandidate(
        val layout: GroupLayout,
        val rowIndex: Int,
        val exact: Boolean,
        val tokenCount: Int,
    )

    private companion object {
        const val HEADER_SCAN_MAX_ROW = 20
        val TIME_RANGE_REGEX = Regex("\\b\\d{1,2}[.:]\\d{2}\\s*[-]\\s*\\d{1,2}[.:]\\d{2}\\b")
    }
}

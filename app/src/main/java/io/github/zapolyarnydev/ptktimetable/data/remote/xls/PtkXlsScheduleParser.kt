package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress
import java.io.ByteArrayInputStream
import java.util.Locale

class PtkXlsScheduleParser {

    fun parseSchedule(xlsBytes: ByteArray, groupName: String): List<PtkRawLesson> {
        if (xlsBytes.isEmpty()) return emptyList()
        val normalizedGroupName = normalize(groupName)
        if (normalizedGroupName.isBlank()) return emptyList()

        val formatter = DataFormatter(Locale.forLanguageTag("ru-RU"))
        val lessons = mutableListOf<PtkRawLesson>()

        HSSFWorkbook(ByteArrayInputStream(xlsBytes)).use { workbook ->
            workbook.forEach { sheet ->
                lessons += parseSheet(sheet, normalizedGroupName, formatter)
            }
        }

        val distinct = lessons.distinctBy {
            "${it.groupName}|${it.dayOfWeek}|${it.timeRange}|${it.rawText}|${it.weekType}"
        }
        return removeAllWhenSpecificWeeksExist(distinct)
    }

    private fun parseSheet(
        sheet: Sheet,
        normalizedGroupName: String,
        formatter: DataFormatter
    ): List<PtkRawLesson> {
        val layout = findGroupLayout(sheet, normalizedGroupName, formatter) ?: return emptyList()
        val timeRows = collectTimeRows(sheet, layout.timeColumn, formatter)
        if (timeRows.isEmpty()) return emptyList()

        val result = mutableListOf<PtkRawLesson>()
        var currentDay = ""

        timeRows.forEachIndexed { index, rowIndex ->
            val timeRange = normalize(getCellText(sheet, rowIndex, layout.timeColumn, formatter))
            if (!isTimeRange(timeRange)) return@forEachIndexed

            val dayCandidate = normalize(getCellText(sheet, rowIndex, layout.dayColumn, formatter))
            if (isDayOfWeek(dayCandidate)) currentDay = dayCandidate
            if (currentDay.isBlank()) return@forEachIndexed

            val nextTimeRow = timeRows.getOrNull(index + 1) ?: (sheet.lastRowNum + 1)
            val slotBottomRow = minOf(rowIndex + 1, nextTimeRow - 1)
            val hasTwoRows = slotBottomRow > rowIndex

            val topText = normalize(getCellText(sheet, rowIndex, layout.lessonColumn, formatter))
            val bottomText = if (hasTwoRows) {
                normalize(getCellText(sheet, slotBottomRow, layout.lessonColumn, formatter))
            } else {
                ""
            }

            val isSplitByRows = hasTwoRows && !isVerticallyMerged(
                sheet = sheet,
                rowIndex = rowIndex,
                columnIndex = layout.lessonColumn,
                targetRow = slotBottomRow
            )

            val lessonsForSlot = if (isSplitByRows) {
                mapSplitRows(topText, bottomText)
            } else {
                mapSingleCell(topText, bottomText)
            }

            lessonsForSlot.forEach { (lessonText, weekType) ->
                if (!hasLessonText(lessonText)) return@forEach
                if (isHeaderNoise(lessonText)) return@forEach

                result += PtkRawLesson(
                    groupName = normalizedGroupName,
                    dayOfWeek = currentDay,
                    timeRange = timeRange,
                    rawText = lessonText,
                    weekType = weekType
                )
            }
        }

        return result
    }

    private fun findGroupLayout(
        sheet: Sheet,
        normalizedGroupName: String,
        formatter: DataFormatter
    ): GroupLayout? {
        val maxColumn = findMaxColumn(sheet)
        val maxHeaderRow = minOf(sheet.lastRowNum, HEADER_SCAN_MAX_ROW)
        val candidates = mutableListOf<BlockCandidate>()

        var blockStart = 0
        while (blockStart + 2 <= maxColumn) {
            val dayColumn = blockStart
            val timeColumn = blockStart + 1
            val lessonColumn = blockStart + 2

            for (rowIndex in 0..maxHeaderRow) {
                for (columnIndex in blockStart..lessonColumn) {
                    val text = normalize(getCellText(sheet, rowIndex, columnIndex, formatter))
                    if (!containsGroupToken(text, normalizedGroupName)) continue

                    candidates += BlockCandidate(
                        layout = GroupLayout(dayColumn, timeColumn, lessonColumn),
                        rowIndex = rowIndex,
                        exact = text == normalizedGroupName,
                        tokenCount = tokenize(text).size
                    )
                }
            }

            blockStart += 3
        }

        if (candidates.isEmpty()) return null

        return candidates
            .sortedWith(
                compareByDescending<BlockCandidate> { it.exact }
                    .thenBy { it.tokenCount }
                    .thenBy { it.rowIndex }
            )
            .first()
            .layout
    }

    private fun collectTimeRows(
        sheet: Sheet,
        timeColumn: Int,
        formatter: DataFormatter
    ): List<Int> {
        val rows = mutableListOf<Int>()
        for (rowIndex in 0..sheet.lastRowNum) {
            val merged = findMergedRegion(sheet, rowIndex, timeColumn)
            if (merged != null && merged.firstRow < rowIndex) continue

            val value = normalize(getCellText(sheet, rowIndex, timeColumn, formatter))
            if (isTimeRange(value)) rows += rowIndex
        }
        return rows
    }

    private fun mapSplitRows(topText: String, bottomText: String): List<Pair<String, PtkWeekType>> {
        val topHasLesson = hasLessonText(topText)
        val bottomHasLesson = hasLessonText(bottomText)

        return when {
            topHasLesson && bottomHasLesson -> listOf(
                topText to PtkWeekType.UPPER,
                bottomText to PtkWeekType.LOWER
            )
            topHasLesson -> listOf(topText to PtkWeekType.UPPER)
            bottomHasLesson -> listOf(bottomText to PtkWeekType.LOWER)
            else -> emptyList()
        }
    }

    private fun mapSingleCell(topText: String, bottomText: String): List<Pair<String, PtkWeekType>> {
        val firstText = listOf(topText, bottomText).firstOrNull { hasLessonText(it) } ?: return emptyList()
        return listOf(firstText to PtkWeekType.ALL)
    }

    private fun getCellText(
        sheet: Sheet,
        rowIndex: Int,
        columnIndex: Int,
        formatter: DataFormatter
    ): String {
        if (rowIndex < 0 || columnIndex < 0) return ""

        val directValue = formatCell(sheet, rowIndex, columnIndex, formatter)
        if (directValue.isNotBlank()) return directValue

        val merged = findMergedRegion(sheet, rowIndex, columnIndex) ?: return ""
        return formatCell(sheet, merged.firstRow, merged.firstColumn, formatter)
    }

    private fun formatCell(
        sheet: Sheet,
        rowIndex: Int,
        columnIndex: Int,
        formatter: DataFormatter
    ): String {
        val row = sheet.getRow(rowIndex) ?: return ""
        val cell = row.getCell(columnIndex) ?: return ""
        if (cell.cellType == CellType.BLANK) return ""
        return formatter.formatCellValue(cell).trim()
    }

    private fun findMergedRegion(
        sheet: Sheet,
        rowIndex: Int,
        columnIndex: Int
    ): CellRangeAddress? {
        for (i in 0 until sheet.numMergedRegions) {
            val region = sheet.getMergedRegion(i)
            if (region.isInRange(rowIndex, columnIndex)) return region
        }
        return null
    }

    private fun isVerticallyMerged(
        sheet: Sheet,
        rowIndex: Int,
        columnIndex: Int,
        targetRow: Int
    ): Boolean {
        val region = findMergedRegion(sheet, rowIndex, columnIndex) ?: return false
        return region.firstRow <= targetRow && region.lastRow >= targetRow
    }

    private fun findMaxColumn(sheet: Sheet): Int {
        var max = 0
        for (r in 0..sheet.lastRowNum) {
            val row = sheet.getRow(r) ?: continue
            val last = row.lastCellNum.toInt() - 1
            if (last > max) max = last
        }
        return max
    }

    private fun isDayOfWeek(value: String): Boolean {
        val normalized = value.lowercase(Locale.ROOT).replace('ё', 'е')
        return DAY_KEYWORDS.any { normalized.contains(it) }
    }

    private fun isTimeRange(value: String): Boolean {
        if (value.isBlank()) return false
        return TIME_RANGE_REGEX.containsMatchIn(value.replace('—', '-').replace('–', '-'))
    }

    private fun isHeaderNoise(value: String): Boolean {
        val normalized = value.lowercase(Locale.ROOT).replace('ё', 'е')
        return normalized.contains("занятия") ||
            normalized.contains("день недели") ||
            normalized.contains("зам директора")
    }

    private fun containsGroupToken(cellText: String, normalizedGroupName: String): Boolean {
        if (normalize(cellText) == normalizedGroupName) return true
        return tokenize(cellText).any { it == normalizedGroupName }
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.ROOT)
            .split(Regex("[^\\p{L}\\p{Nd}]+"))
            .filter { it.isNotBlank() }
    }

    private fun hasLessonText(value: String): Boolean {
        val normalized = normalize(value)
        if (normalized.isBlank()) return false
        return !DASH_ONLY_REGEX.matches(normalized)
    }

    private fun normalize(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private fun removeAllWhenSpecificWeeksExist(lessons: List<PtkRawLesson>): List<PtkRawLesson> {
        val grouped = lessons.groupBy { "${it.groupName}|${it.dayOfWeek}|${it.timeRange}|${it.rawText}" }
        return grouped.values.flatMap { sameSlot ->
            val hasSpecific = sameSlot.any { it.weekType == PtkWeekType.UPPER || it.weekType == PtkWeekType.LOWER }
            if (hasSpecific) sameSlot.filterNot { it.weekType == PtkWeekType.ALL } else sameSlot
        }
    }

    private data class GroupLayout(
        val dayColumn: Int,
        val timeColumn: Int,
        val lessonColumn: Int
    )

    private data class BlockCandidate(
        val layout: GroupLayout,
        val rowIndex: Int,
        val exact: Boolean,
        val tokenCount: Int
    )

    private companion object {
        const val HEADER_SCAN_MAX_ROW = 20
        val TIME_RANGE_REGEX = Regex("\\b\\d{1,2}[.:]\\d{2}\\s*[-]\\s*\\d{1,2}[.:]\\d{2}\\b")
        val DASH_ONLY_REGEX = Regex("^[-—–]+$")

        val DAY_KEYWORDS = listOf(
            "понедельник",
            "вторник",
            "среда",
            "четверг",
            "пятница",
            "суббота",
            "воскресенье",
            "пн",
            "вт",
            "ср",
            "чт",
            "пт",
            "сб",
            "вс"
        )
    }
}

package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import io.github.zapolyarnydev.ptktimetable.data.model.PtkRawLesson
import io.github.zapolyarnydev.ptktimetable.data.model.PtkWeekType
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.BorderStyle
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
            val lessonsForSlot = mapSlotLessons(
                sheet = sheet,
                lessonColumn = layout.lessonColumn,
                rowIndex = rowIndex,
                nextTimeRow = nextTimeRow,
                formatter = formatter
            )

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

    private fun mapSlotLessons(
        sheet: Sheet,
        lessonColumn: Int,
        rowIndex: Int,
        nextTimeRow: Int,
        formatter: DataFormatter
    ): List<Pair<String, PtkWeekType>> {
        val rawSegments = mutableListOf<RowSegment>()
        for (lessonRow in rowIndex until nextTimeRow) {
            val rawText = getCellText(sheet, lessonRow, lessonColumn, formatter)
            val text = normalize(rawText)
            if (text.isBlank()) continue
            if (rawSegments.lastOrNull()?.text == text) continue
            rawSegments += RowSegment(lessonRow, text, rawText)
        }

        val scheduleSegments = rawSegments.filterNot { isHeaderNoise(it.text) }
        if (scheduleSegments.isEmpty()) return emptyList()

        if (isSplitSlot(sheet, lessonColumn, rowIndex, nextTimeRow)) {
            return if (scheduleSegments.size >= 2) {
                mapSplitRows(scheduleSegments[0].text, scheduleSegments[1].text)
            } else {
                val single = scheduleSegments.first()
                val dashedBefore = hasDashedDividerBeforeRow(
                    sheet = sheet,
                    lessonColumn = lessonColumn,
                    fromRow = rowIndex,
                    toRowExclusive = single.rowIndex
                )
                val dashedOnSegment = hasDashedBottomAtRow(
                    sheet = sheet,
                    lessonColumn = lessonColumn,
                    rowIndex = single.rowIndex
                )
                if (dashedBefore || dashedOnSegment) return listOf(single.text to PtkWeekType.LOWER)
                val splitPoint = rowIndex + ((nextTimeRow - rowIndex) / 2)
                if (single.rowIndex >= splitPoint) {
                    listOf(single.text to PtkWeekType.LOWER)
                } else {
                    listOf(single.text to PtkWeekType.ALL)
                }
            }
        }

        return if (scheduleSegments.size >= 2) {
            mapSplitRows(scheduleSegments[0].text, scheduleSegments[1].text)
        } else {
            val segment = scheduleSegments.first()
            val dashedBefore = hasDashedDividerBeforeRow(
                sheet = sheet,
                lessonColumn = lessonColumn,
                fromRow = rowIndex,
                toRowExclusive = segment.rowIndex
            )
            mapSingleCell(
                segment = segment,
                sheet = sheet,
                lessonColumn = lessonColumn,
                preferLowerForAmbiguous = dashedBefore
            )
        }
    }

    private fun hasDashedDividerBeforeRow(
        sheet: Sheet,
        lessonColumn: Int,
        fromRow: Int,
        toRowExclusive: Int
    ): Boolean {
        if (fromRow >= toRowExclusive) return false
        for (rowIndex in fromRow until toRowExclusive) {
            val row = sheet.getRow(rowIndex)
            val cell = row?.getCell(lessonColumn)
            val style = cell?.cellStyle ?: continue
            if (isDashedBorder(style.borderBottom)) {
                return true
            }
        }
        return false
    }

    private fun hasDashedBottomAtRow(
        sheet: Sheet,
        lessonColumn: Int,
        rowIndex: Int
    ): Boolean {
        val row = sheet.getRow(rowIndex) ?: return false
        val cell = row.getCell(lessonColumn) ?: return false
        val borderBottom = cell.cellStyle?.borderBottom ?: return false
        return isDashedBorder(borderBottom)
    }

    private fun isSplitSlot(
        sheet: Sheet,
        lessonColumn: Int,
        rowIndex: Int,
        nextTimeRow: Int
    ): Boolean {
        val slotHeight = nextTimeRow - rowIndex
        if (slotHeight < 2) return false

        val merged = findMergedRegion(sheet, rowIndex, lessonColumn)
        if (merged != null && merged.firstRow <= rowIndex && merged.lastRow >= nextTimeRow - 1) {
            return false
        }
        return true
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

    private fun mapSingleCell(
        segment: RowSegment,
        sheet: Sheet,
        lessonColumn: Int,
        preferLowerForAmbiguous: Boolean
    ): List<Pair<String, PtkWeekType>> {
        val text = segment.text
        if (!hasLessonText(text)) return emptyList()
        if (looksLikeLowerOnlyCell(segment.rawText)) return listOf(text to PtkWeekType.LOWER)
        if (preferLowerForAmbiguous) return listOf(text to PtkWeekType.LOWER)
        return listOf(text to PtkWeekType.ALL)
    }

    private fun looksLikeLowerOnlyCell(rawText: String): Boolean {
        val lines = rawText.replace("\r", "").split('\n')
        if (lines.size < 2) return false
        val firstNonBlank = lines.indexOfFirst { normalize(it).isNotBlank() && !DASH_ONLY_REGEX.matches(normalize(it)) }
        return firstNonBlank > 0
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
        return formatter.formatCellValue(cell)
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
        val normalized = value.replace('—', '-').replace('–', '-')
        return TIME_RANGE_REGEX.containsMatchIn(normalized)
    }

    private fun isHeaderNoise(value: String): Boolean {
        val normalized = value.lowercase(Locale.ROOT)
            .replace('ё', 'е')
            .replace(".", "")
            .replace(",", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

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

    private fun isDashedBorder(borderStyle: BorderStyle): Boolean {
        return borderStyle == BorderStyle.DASHED ||
            borderStyle == BorderStyle.DOTTED ||
            borderStyle == BorderStyle.MEDIUM_DASHED ||
            borderStyle == BorderStyle.MEDIUM_DASH_DOT ||
            borderStyle == BorderStyle.MEDIUM_DASH_DOT_DOT ||
            borderStyle == BorderStyle.SLANTED_DASH_DOT
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

    private data class RowSegment(
        val rowIndex: Int,
        val text: String,
        val rawText: String
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

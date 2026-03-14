package io.github.zapolyarnydev.ptktimetable.data.remote.parser

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

        return lessons.distinctBy {
            "${it.groupName}|${it.dayOfWeek}|${it.timeRange}|${it.rawText}|${it.weekType}"
        }
    }

    private fun parseSheet(
        sheet: Sheet,
        normalizedGroupName: String,
        formatter: DataFormatter
    ): List<PtkRawLesson> {
        val groupColumns = findGroupColumns(sheet, normalizedGroupName, formatter)
        if (groupColumns.isEmpty()) return emptyList()

        val startRow = groupColumns.minOf { it.headerRow } + 1
        val result = mutableListOf<PtkRawLesson>()
        var currentDay = ""
        var currentTime = ""

        for (rowIndex in startRow..sheet.lastRowNum) {
            val dayCandidate = normalize(getCellText(sheet, rowIndex, DAY_COLUMN, formatter))
            if (isDayOfWeek(dayCandidate)) {
                currentDay = dayCandidate
            }

            val timeCandidate = normalize(getCellText(sheet, rowIndex, TIME_COLUMN, formatter))
            if (isTimeRange(timeCandidate)) {
                currentTime = timeCandidate
            }

            if (currentDay.isBlank() || currentTime.isBlank()) continue

            groupColumns.forEach { groupColumn ->
                val rawLesson = normalize(getCellText(sheet, rowIndex, groupColumn.columnIndex, formatter))
                if (rawLesson.isBlank()) return@forEach
                if (rawLesson == normalizedGroupName) return@forEach
                if (isHeaderNoise(rawLesson)) return@forEach

                result += PtkRawLesson(
                    groupName = normalizedGroupName,
                    dayOfWeek = currentDay,
                    timeRange = currentTime,
                    rawText = rawLesson,
                    weekType = groupColumn.weekType
                )
            }
        }

        return result
    }

    private fun findGroupColumns(
        sheet: Sheet,
        normalizedGroupName: String,
        formatter: DataFormatter
    ): List<GroupColumn> {
        val map = linkedMapOf<Int, GroupColumn>()
        val maxRowToScan = minOf(sheet.lastRowNum, 120)

        for (rowIndex in 0..maxRowToScan) {
            val row = sheet.getRow(rowIndex) ?: continue
            val firstCell = if (row.firstCellNum >= 0) row.firstCellNum.toInt() else 0
            val lastCell = if (row.lastCellNum >= 0) row.lastCellNum.toInt() else 0

            for (columnIndex in firstCell..lastCell) {
                val text = normalize(getCellText(sheet, rowIndex, columnIndex, formatter))
                if (!containsGroupToken(text, normalizedGroupName)) continue

                val mergedRegion = findMergedRegion(sheet, rowIndex, columnIndex)
                val columns = if (mergedRegion != null && mergedRegion.lastColumn > mergedRegion.firstColumn) {
                    (mergedRegion.firstColumn..mergedRegion.lastColumn).toList()
                } else {
                    listOf(columnIndex)
                }

                val guessedWeekTypes = columns.map { col ->
                    col to inferWeekType(sheet, rowIndex, col, formatter)
                }

                val resolvedWeekTypes = if (
                    columns.size == 2 &&
                    guessedWeekTypes.all { it.second == PtkWeekType.ALL }
                ) {
                    listOf(
                        columns[0] to PtkWeekType.UPPER,
                        columns[1] to PtkWeekType.LOWER
                    )
                } else {
                    guessedWeekTypes
                }

                resolvedWeekTypes.forEach { (col, weekType) ->
                    val existing = map[col]
                    if (existing == null || (existing.weekType == PtkWeekType.ALL && weekType != PtkWeekType.ALL)) {
                        map[col] = GroupColumn(
                            headerRow = rowIndex,
                            columnIndex = col,
                            weekType = weekType
                        )
                    }
                }
            }
        }

        return map.values.toList()
    }

    private fun inferWeekType(
        sheet: Sheet,
        headerRowIndex: Int,
        columnIndex: Int,
        formatter: DataFormatter
    ): PtkWeekType {
        val checkRows = (headerRowIndex - 1..headerRowIndex + 2)
        checkRows.forEach { rowIndex ->
            if (rowIndex < 0) return@forEach
            val value = normalize(getCellText(sheet, rowIndex, columnIndex, formatter))
            val fromValue = detectWeekType(value)
            if (fromValue != PtkWeekType.ALL) return fromValue
        }

        val neighbors = listOf(columnIndex - 1, columnIndex + 1)
        neighbors.forEach { neighborColumn ->
            if (neighborColumn < 0) return@forEach
            val value = normalize(getCellText(sheet, headerRowIndex + 1, neighborColumn, formatter))
            val fromValue = detectWeekType(value)
            if (fromValue != PtkWeekType.ALL) return fromValue
        }

        return PtkWeekType.ALL
    }

    private fun detectWeekType(text: String): PtkWeekType {
        val normalized = text.lowercase(Locale.ROOT).replace('ё', 'е')
        return when {
            normalized.contains("верх") || normalized.contains("нечет") -> PtkWeekType.UPPER
            normalized.contains("ниж") || normalized.contains("чет") -> PtkWeekType.LOWER
            else -> PtkWeekType.ALL
        }
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

    private fun isDayOfWeek(value: String): Boolean {
        val normalized = value.lowercase(Locale.ROOT).replace('ё', 'е')
        return DAY_KEYWORDS.any { normalized.contains(it) }
    }

    private fun isTimeRange(value: String): Boolean {
        if (value.isBlank()) return false
        return TIME_RANGE_REGEX.containsMatchIn(value.replace('—', '-').replace('–', '-'))
    }

    private fun isHeaderNoise(value: String): Boolean {
        val normalized = value.lowercase(Locale.ROOT)
        return normalized.contains("занятия") || normalized.contains("день недели")
    }

    private fun containsGroupToken(cellText: String, normalizedGroupName: String): Boolean {
        if (cellText == normalizedGroupName) return true
        val tokens = cellText
            .lowercase(Locale.ROOT)
            .split(Regex("[^\\p{L}\\p{Nd}]+"))
            .filter { it.isNotBlank() }
        return tokens.any { it == normalizedGroupName }
    }

    private fun normalize(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private data class GroupColumn(
        val headerRow: Int,
        val columnIndex: Int,
        val weekType: PtkWeekType
    )

    private companion object {
        const val DAY_COLUMN = 0
        const val TIME_COLUMN = 1

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

        val TIME_RANGE_REGEX =
            Regex("\\b\\d{1,2}[.:]\\d{2}\\s*[-]\\s*\\d{1,2}[.:]\\d{2}\\b")
    }
}

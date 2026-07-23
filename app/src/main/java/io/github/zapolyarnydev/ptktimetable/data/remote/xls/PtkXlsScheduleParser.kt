package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import io.github.zapolyarnydev.ptktimetable.domain.schedule.model.WeekType
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import java.io.ByteArrayInputStream
import java.util.Locale

class PtkXlsScheduleParser : ScheduleDocumentParser {

    override fun parseSchedule(bytes: ByteArray, groupName: String): List<NovsuRawLesson> {
        if (bytes.isEmpty()) return emptyList()
        val normalizedGroupName = normalize(groupName)
        if (normalizedGroupName.isBlank()) return emptyList()

        val lessons = mutableListOf<NovsuRawLesson>()
        HSSFWorkbook(ByteArrayInputStream(bytes)).use { workbook ->
            val reader = XlsSheetReader(DataFormatter(Locale.forLanguageTag("ru-RU")))
            val layoutDetector = PtkSheetLayoutDetector(reader)
            val slotInterpreter = PtkLessonSlotInterpreter(reader)
            workbook.forEach { sheet ->
                lessons += parseSheet(sheet, normalizedGroupName, reader, layoutDetector, slotInterpreter)
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
        reader: XlsSheetReader,
        layoutDetector: PtkSheetLayoutDetector,
        slotInterpreter: PtkLessonSlotInterpreter,
    ): List<NovsuRawLesson> {
        val layout = layoutDetector.find(sheet, normalizedGroupName) ?: return emptyList()
        val timeRows = layoutDetector.timeRows(sheet, layout.timeColumn)
        if (timeRows.isEmpty()) return emptyList()

        val result = mutableListOf<NovsuRawLesson>()
        var currentDay = ""

        timeRows.forEachIndexed { index, rowIndex ->
            val timeRange = normalize(reader.text(sheet, rowIndex, layout.timeColumn))
            val dayCandidate = normalize(reader.text(sheet, rowIndex, layout.dayColumn))
            if (isDayOfWeek(dayCandidate)) currentDay = dayCandidate
            if (currentDay.isBlank()) return@forEachIndexed

            val nextTimeRow = timeRows.getOrNull(index + 1) ?: (sheet.lastRowNum + 1)
            slotInterpreter.read(sheet, layout.lessonColumn, rowIndex, nextTimeRow)
                .forEach { (lessonText, weekType) ->
                    if (!slotInterpreter.hasLessonText(lessonText) || slotInterpreter.isHeaderNoise(lessonText)) {
                        return@forEach
                    }
                    result += NovsuRawLesson(
                        groupName = normalizedGroupName,
                        dayOfWeek = currentDay,
                        timeRange = timeRange,
                        rawText = lessonText,
                        weekType = weekType,
                    )
                }
        }
        return result
    }

    private fun isDayOfWeek(value: String): Boolean {
        val normalized = value.lowercase(Locale.ROOT).replace('ё', 'е')
        return DAY_KEYWORDS.any { normalized.contains(it) }
    }

    private fun removeAllWhenSpecificWeeksExist(lessons: List<NovsuRawLesson>): List<NovsuRawLesson> {
        val grouped = lessons.groupBy { "${it.groupName}|${it.dayOfWeek}|${it.timeRange}|${it.rawText}" }
        return grouped.values.flatMap { sameSlot ->
            val hasSpecific = sameSlot.any { it.weekType == WeekType.UPPER || it.weekType == WeekType.LOWER }
            if (hasSpecific) sameSlot.filterNot { it.weekType == WeekType.ALL } else sameSlot
        }
    }

    private fun normalize(value: String): String = value.replace(Regex("\\s+"), " ").trim()

    private companion object {
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
            "вс",
        )
    }
}

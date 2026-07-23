package io.github.zapolyarnydev.ptktimetable.data.remote.xls

interface ScheduleDocumentParser {
    fun parseSchedule(bytes: ByteArray, groupName: String): List<NovsuRawLesson>
}

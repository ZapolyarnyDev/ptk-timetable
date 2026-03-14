package io.github.zapolyarnydev.ptktimetable.data.remote

import io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleParsingIntegrationTest {

    private val htmlParser = PtkGroupsHtmlParser()
    private val xlsParser = PtkXlsScheduleParser()

    @Test
    fun `parses groups from html and then parses schedule from corresponding xls`() {
        val html = loadResourceText("html/portal_ptk_valid.html")
        val groups = htmlParser.parseGroups(html, BASE_URL)
        val group = groups.firstOrNull { it.groupName == "3781" }
            ?: error("Expected group 3781 in parsed HTML groups")

        val xlsBytes = loadResourceBytes("xls/3781 3782.xls")
        val lessons = xlsParser.parseSchedule(xlsBytes, group.groupName)

        assertTrue("Expected non-empty lessons for ${group.groupName}", lessons.isNotEmpty())
        assertTrue("All lessons must belong to selected group", lessons.all { it.groupName == group.groupName })
        assertTrue("All lessons must have non-blank day/time/text", lessons.all {
            it.dayOfWeek.isNotBlank() && it.timeRange.isNotBlank() && it.rawText.isNotBlank()
        })
    }

    private fun loadResourceText(path: String): String {
        val stream = javaClass.classLoader?.getResourceAsStream(path)
            ?: error("Resource not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }

    private fun loadResourceBytes(path: String): ByteArray {
        val stream = javaClass.classLoader?.getResourceAsStream(path)
            ?: error("Resource not found: $path")
        return stream.use { it.readBytes() }
    }

    private companion object {
        const val BASE_URL = "https://portal.novsu.ru/univer/timetable/spo/"
    }
}

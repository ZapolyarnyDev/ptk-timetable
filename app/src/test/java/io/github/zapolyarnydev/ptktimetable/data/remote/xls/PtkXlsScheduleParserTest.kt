package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method

class PtkXlsScheduleParserTest {

    private val sut = ParserContract(
        parserClassName = "io.github.zapolyarnydev.ptktimetable.data.remote.xls.PtkXlsScheduleParser",
        parseMethodName = "parseSchedule"
    )

    @Test
    fun `parseSchedule parses non empty schedule for group 3781`() {
        val xlsBytes = loadXls("xls/3781 3782.xls")

        val lessons = sut.parse(xlsBytes, "3781")

        assertTrue("Expected at least one lesson for group 3781", lessons.isNotEmpty())
        assertTrue("All parsed rows must belong to requested group", lessons.all { it.groupName == "3781" })
        assertTrue("dayOfWeek must be non-blank", lessons.all { it.dayOfWeek.isNotBlank() })
        assertTrue("timeRange must be non-blank", lessons.all { it.timeRange.isNotBlank() })
        assertTrue("rawText must be non-blank", lessons.all { it.rawText.isNotBlank() })
    }

    @Test
    fun `parseSchedule parses second group from same workbook`() {
        val xlsBytes = loadXls("xls/3781 3782.xls")

        val lessons = sut.parse(xlsBytes, "3782")

        assertTrue("Expected at least one lesson for group 3782", lessons.isNotEmpty())
        assertTrue("All parsed rows must belong to requested group", lessons.all { it.groupName == "3782" })
    }

    @Test
    fun `parseSchedule parses groups from multi group workbook`() {
        val xlsBytes = loadXls("xls/3991 3992 3993 3994.xls")

        val group3991 = sut.parse(xlsBytes, "3991")
        val group3994 = sut.parse(xlsBytes, "3994")

        assertTrue("Expected lessons for group 3991", group3991.isNotEmpty())
        assertTrue("Expected lessons for group 3994", group3994.isNotEmpty())
    }

    @Test
    fun `parseSchedule detects upper and lower week lessons for split cells`() {
        val xlsBytes = loadXls("xls/3991 3992 3993 3994.xls")

        val lessons = sut.parse(xlsBytes, "3993")
        val weekTypes = lessons.map { it.weekType }.toSet()

        assertTrue("Expected at least one upper-week lesson", weekTypes.contains("UPPER"))
        assertTrue("Expected at least one lower-week lesson", weekTypes.contains("LOWER"))
    }

    @Test
    fun `parseSchedule matches expected thursday structure for group 3993`() {
        val xlsBytes = loadXls("xls/3991 3992 3993 3994.xls")
        val lessons = sut.parse(xlsBytes, "3993")

        val thursday = lessons.filter { it.dayOfWeek.contains("четверг", ignoreCase = true) }

        val slot1 = thursday.filter { it.timeRange == "8.30-10.10" }
        assertTrue(slot1.any { it.weekType == "UPPER" && it.rawText.contains("WordPress", ignoreCase = true) })
        assertTrue(slot1.any { it.weekType == "LOWER" && it.rawText.contains("Тестирование", ignoreCase = true) })

        val slot2 = thursday.filter { it.timeRange == "10.20-12.00" }
        assertTrue(slot2.any { it.weekType == "ALL" && it.rawText.contains("Тестирование", ignoreCase = true) })

        val slot3 = thursday.filter { it.timeRange == "12.45-14.25" }
        assertTrue(slot3.any { it.weekType == "ALL" && it.rawText.contains("Java", ignoreCase = true) })

        val slot4 = thursday.filter { it.timeRange == "14.35-16.15" }
        assertTrue(slot4.any { it.weekType == "ALL" && it.rawText.contains("Проектирование", ignoreCase = true) })

        val slot5 = thursday.filter { it.timeRange == "16.25-18.05" }
        assertTrue(slot5.isEmpty())
    }

    @Test
    fun `parseSchedule returns empty when group is absent in workbook`() {
        val xlsBytes = loadXls("xls/3781 3782.xls")

        val lessons = sut.parse(xlsBytes, "9999")

        assertTrue("Expected empty list when group is absent", lessons.isEmpty())
    }

    private fun loadXls(path: String): ByteArray {
        val stream = javaClass.classLoader?.getResourceAsStream(path)
            ?: error("XLS fixture not found: $path")
        return stream.use { it.readBytes() }
    }

    private data class LessonRef(
        val groupName: String,
        val dayOfWeek: String,
        val timeRange: String,
        val rawText: String,
        val weekType: String
    )

    private class ParserContract(
        parserClassName: String,
        parseMethodName: String
    ) {
        private val parserInstance: Any
        private val parseMethod: Method

        init {
            val parserClass = try {
                Class.forName(parserClassName)
            } catch (e: ClassNotFoundException) {
                throw AssertionError(
                    "Expected parser class `$parserClassName` not found. " +
                        "Create it to satisfy TDD tests.",
                    e
                )
            }
            parserInstance = parserClass.getDeclaredConstructor().newInstance()
            parseMethod = parserClass.methods.firstOrNull { method ->
                method.name == parseMethodName && method.parameterCount == 2
            } ?: throw AssertionError(
                "Expected method `$parseMethodName(xlsBytes: ByteArray, groupName: String)` " +
                    "in `$parserClassName` was not found."
            )
        }

        fun parse(xlsBytes: ByteArray, groupName: String): List<LessonRef> {
            val raw = parseMethod.invoke(parserInstance, xlsBytes, groupName)
                ?: throw AssertionError("parseSchedule returned null, expected List")
            val list = raw as? List<*>
                ?: throw AssertionError("parseSchedule should return List, actual=${raw::class.java.name}")
            return list.map { toLessonRef(it) }
        }

        private fun toLessonRef(item: Any?): LessonRef {
            val raw = item ?: throw AssertionError("List contains null element")
            val groupName = readString(raw, "groupName")
            val dayOfWeek = readString(raw, "dayOfWeek")
            val timeRange = readString(raw, "timeRange")
            val rawText = readString(raw, "rawText")
            val weekType = readProperty(raw, "weekType")?.toString()
                ?: throw AssertionError("Property `weekType` must not be null")
            return LessonRef(groupName, dayOfWeek, timeRange, rawText, weekType)
        }

        private fun readString(raw: Any, property: String): String {
            val value = readProperty(raw, property)
            return value as? String ?: throw AssertionError(
                "Property `$property` must be String, actual=${value?.javaClass?.name}"
            )
        }

        private fun readProperty(raw: Any, property: String): Any? {
            val getterName = "get" + property.replaceFirstChar { it.uppercaseChar() }
            val getter = raw.javaClass.methods.firstOrNull { it.name == getterName && it.parameterCount == 0 }
                ?: throw AssertionError(
                    "Expected property `$property` (getter `$getterName`) in `${raw.javaClass.name}`"
                )
            return getter.invoke(raw)
        }
    }
}

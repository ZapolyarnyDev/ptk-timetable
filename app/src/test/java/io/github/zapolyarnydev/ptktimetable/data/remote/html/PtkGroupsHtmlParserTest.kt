package io.github.zapolyarnydev.ptktimetable.data.remote.html

import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method

class PtkGroupsHtmlParserTest {

    private val sut = ParserContract(
        parserClassName = "io.github.zapolyarnydev.ptktimetable.data.remote.html.PtkGroupsHtmlParser",
        parseMethodName = "parseGroups"
    )

    @Test
    fun `parseGroups extracts non empty group list from PTK section`() {
        val html = loadResource("html/portal_ptk_valid.html")
        val result = sut.parse(html, BASE_URL)

        assertTrue("Expected at least one parsed PTK group", result.isNotEmpty())
    }

    @Test
    fun `parseGroups keeps only PTK xls links and resolves them to absolute url`() {
        val html = loadResource("html/portal_ptk_valid.html")
        val result = sut.parse(html, BASE_URL)

        assertTrue("Expected at least one parsed PTK group", result.isNotEmpty())
        assertTrue(
            "Every parsed link must point to PTK xls file with absolute URL",
            result.all { item ->
                item.xlsUrl.startsWith("https://portal.novsu.ru/") &&
                    item.xlsUrl.contains("/_timetable/ptk/") &&
                    item.xlsUrl.contains(".xls")
            }
        )
    }

    @Test
    fun `parseGroups parses course from table column and group labels are not blank`() {
        val html = loadResource("html/portal_ptk_valid.html")
        val result = sut.parse(html, BASE_URL)

        assertTrue("Expected at least one parsed PTK group", result.isNotEmpty())
        assertTrue("Each group name must be non-blank", result.all { it.groupName.isNotBlank() })
        assertTrue("Course must be in 1..7", result.all { it.course in 1..7 })
        assertTrue("Expected to parse at least one course value", result.map { it.course }.toSet().isNotEmpty())
    }

    @Test
    fun `parseGroups returns empty list when Политехнический колледж section is absent`() {
        val html = loadResource("html/portal_no_ptk.html")
        val result = sut.parse(html, BASE_URL)

        assertTrue(result.isEmpty())
    }

    private fun loadResource(path: String): String {
        val stream = javaClass.classLoader?.getResourceAsStream(path)
            ?: error("Test fixture not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }

    private data class GroupRef(
        val groupName: String,
        val course: Int,
        val xlsUrl: String
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
                "Expected method `$parseMethodName(html: String, baseUrl: String)` " +
                    "in `$parserClassName` was not found."
            )
        }

        fun parse(html: String, baseUrl: String): List<GroupRef> {
            val raw = parseMethod.invoke(parserInstance, html, baseUrl)
                ?: throw AssertionError("parseGroups returned null, expected List")
            val list = raw as? List<*>
                ?: throw AssertionError("parseGroups should return List, actual=${raw::class.java.name}")
            return list.map { toGroupRef(it) }
        }

        private fun toGroupRef(item: Any?): GroupRef {
            val raw = item ?: throw AssertionError("List contains null element")
            val groupName = readString(raw, "groupName")
            val course = readInt(raw, "course")
            val xlsUrl = readString(raw, "xlsUrl")
            return GroupRef(groupName, course, xlsUrl)
        }

        private fun readString(raw: Any, property: String): String {
            val value = readProperty(raw, property)
            return value as? String ?: throw AssertionError(
                "Property `$property` must be String, actual=${value?.javaClass?.name}"
            )
        }

        private fun readInt(raw: Any, property: String): Int {
            val value = readProperty(raw, property)
            return when (value) {
                is Int -> value
                is Number -> value.toInt()
                else -> throw AssertionError(
                    "Property `$property` must be numeric, actual=${value?.javaClass?.name}"
                )
            }
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

    private companion object {
        const val BASE_URL = "https://portal.novsu.ru/univer/timetable/spo/"
    }
}

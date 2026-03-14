package io.github.zapolyarnydev.ptktimetable.data.remote.html

import io.github.zapolyarnydev.ptktimetable.data.model.PtkGroupInfo
import io.github.zapolyarnydev.ptktimetable.data.remote.xls.CourseMeta
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.Locale

class PtkGroupsHtmlParser {

    fun parseGroups(html: String, baseUrl: String): List<PtkGroupInfo> {
        val document = Jsoup.parse(html, baseUrl)
        val root = document.selectFirst("body #body") ?: document.body() ?: return emptyList()

        val collegeHeader = root
            .select("h1, h2, h3")
            .firstOrNull { normalize(it.ownText()) == PTK_COLLEGE_NAME }
            ?: return emptyList()

        val table = findCollegeTable(collegeHeader) ?: return emptyList()
        val rows = table.select("tr")
        if (rows.isEmpty()) return emptyList()

        val headerRow = rows.firstOrNull() ?: return emptyList()
        val courseMetaByColumn = extractCourseMetaByColumn(headerRow)
        val collegeName = normalize(collegeHeader.ownText())

        val result = mutableListOf<PtkGroupInfo>()
        rows.drop(1).forEach { row ->
            row.select("td").forEachIndexed { columnIndex, cell ->
                val courseMeta = courseMetaByColumn[columnIndex]
                val course = courseMeta?.number ?: (columnIndex + 1)
                val courseName = courseMeta?.title.orEmpty()

                cell.select("a[href]").forEach { link ->
                    val href = link.attr("href")
                    if (!isXlsLink(href)) return@forEach

                    val groupName = normalize(link.text())
                    if (groupName.isBlank()) return@forEach

                    val absoluteUrl = link.absUrl("href").ifBlank { href }
                    result += PtkGroupInfo(
                        collegeName = collegeName,
                        course = course,
                        courseName = courseName,
                        groupName = groupName,
                        xlsUrl = absoluteUrl
                    )
                }
            }
        }

        return result.distinctBy { "${it.groupName}|${it.course}|${it.xlsUrl}" }
    }

    private fun findCollegeTable(collegeHeader: Element): Element? {
        collegeHeader.parent()?.selectFirst("table.viewtable")?.let { return it }

        var cursor = collegeHeader.nextElementSibling()
        while (cursor != null) {
            if (cursor.tagName() in setOf("h1", "h2", "h3")) return null
            cursor.selectFirst("table.viewtable")?.let { return it }
            cursor = cursor.nextElementSibling()
        }
        return null
    }

    private fun extractCourseMetaByColumn(headerRow: Element): Map<Int, CourseMeta> {
        val map = mutableMapOf<Int, CourseMeta>()
        headerRow.select("th").forEachIndexed { index, headerCell ->
            val headerText = normalize(headerCell.text())
            val courseNumber = COURSE_REGEX.find(headerText)?.groupValues?.get(1)?.toIntOrNull()
            map[index] = CourseMeta(number = courseNumber, title = headerText)
        }
        return map
    }

    private fun isXlsLink(href: String): Boolean {
        return href.lowercase(Locale.ROOT).contains(".xls")
    }

    private fun normalize(input: String): String {
        return input.replace(Regex("\\s+"), " ").trim()
    }

    private companion object {
        const val PTK_COLLEGE_NAME = "Политехнический колледж"
        val COURSE_REGEX = Regex("(\\d+)\\s*курс", setOf(RegexOption.IGNORE_CASE))
    }
}

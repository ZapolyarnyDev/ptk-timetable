package io.github.zapolyarnydev.ptktimetable.data.remote.xls

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellRangeAddress

internal class XlsSheetReader(private val formatter: DataFormatter) {

    fun text(sheet: Sheet, rowIndex: Int, columnIndex: Int): String {
        if (rowIndex < 0 || columnIndex < 0) return ""
        val directValue = formatCell(sheet, rowIndex, columnIndex)
        if (directValue.isNotBlank()) return directValue
        val merged = mergedRegion(sheet, rowIndex, columnIndex) ?: return ""
        return formatCell(sheet, merged.firstRow, merged.firstColumn)
    }

    fun mergedRegion(sheet: Sheet, rowIndex: Int, columnIndex: Int): CellRangeAddress? {
        for (index in 0 until sheet.numMergedRegions) {
            val region = sheet.getMergedRegion(index)
            if (region.isInRange(rowIndex, columnIndex)) return region
        }
        return null
    }

    fun maxColumn(sheet: Sheet): Int {
        var max = 0
        for (rowIndex in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            val last = row.lastCellNum.toInt() - 1
            if (last > max) max = last
        }
        return max
    }

    private fun formatCell(sheet: Sheet, rowIndex: Int, columnIndex: Int): String {
        val row = sheet.getRow(rowIndex) ?: return ""
        val cell = row.getCell(columnIndex) ?: return ""
        if (cell.cellType == CellType.BLANK) return ""
        return formatter.formatCellValue(cell)
    }
}

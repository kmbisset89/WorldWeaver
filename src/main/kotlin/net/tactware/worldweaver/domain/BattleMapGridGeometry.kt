package net.tactware.worldweaver.domain

internal class BattleMapGridGeometry(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val columns: Int,
    private val rows: Int,
) {
    fun cellAtNormalized(x: Double, y: Double): GridCell? {
        if (!isValid()) {
            return null
        }
        val pixelX = (x * imageWidth).toInt().coerceIn(0, imageWidth - 1)
        val pixelY = (y * imageHeight).toInt().coerceIn(0, imageHeight - 1)
        val column = columnAtPixel(pixelX) ?: return null
        val row = rowAtPixel(pixelY) ?: return null
        return GridCell(column = column, row = row)
    }

    fun normalizedCenter(cell: GridCell): Pair<Double, Double>? {
        if (!isValid() || !contains(cell)) {
            return null
        }
        val columnStart = startPixel(cell.column, columns, imageWidth)
        val rowStart = startPixel(cell.row, rows, imageHeight)
        val columnWidth = spanForIndex(cell.column, columns, imageWidth)
        val rowHeight = spanForIndex(cell.row, rows, imageHeight)
        val centerX = (columnStart + columnWidth / 2.0) / imageWidth.toDouble()
        val centerY = (rowStart + rowHeight / 2.0) / imageHeight.toDouble()
        return centerX to centerY
    }

    fun contains(cell: GridCell): Boolean {
        return cell.column in 0 until columns && cell.row in 0 until rows
    }

    fun pixelRect(cell: GridCell): PixelRect? {
        if (!isValid() || !contains(cell)) {
            return null
        }
        return PixelRect(
            x = startPixel(cell.column, columns, imageWidth),
            y = startPixel(cell.row, rows, imageHeight),
            width = spanForIndex(cell.column, columns, imageWidth),
            height = spanForIndex(cell.row, rows, imageHeight),
        )
    }

    data class PixelRect(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
    )

    private fun isValid(): Boolean {
        return imageWidth > 0 && imageHeight > 0 && columns > 0 && rows > 0
    }

    private fun columnAtPixel(pixelX: Int): Int? {
        return indexAtPixel(pixelX, columns, imageWidth)
    }

    private fun rowAtPixel(pixelY: Int): Int? {
        return indexAtPixel(pixelY, rows, imageHeight)
    }

    private fun indexAtPixel(pixel: Int, count: Int, total: Int): Int? {
        var cursor = 0
        for (index in 0 until count) {
            val span = spanForIndex(index, count, total)
            val end = cursor + span
            if (pixel in cursor until end) {
                return index
            }
            cursor = end
        }
        return null
    }

    private fun startPixel(index: Int, count: Int, total: Int): Int {
        val base = total / count
        val remainder = total % count
        val extraBefore = index.coerceAtMost(remainder)
        return index * base + extraBefore
    }

    private fun spanForIndex(index: Int, count: Int, total: Int): Int {
        val base = total / count
        val remainder = total % count
        return base + if (index < remainder) 1 else 0
    }
}

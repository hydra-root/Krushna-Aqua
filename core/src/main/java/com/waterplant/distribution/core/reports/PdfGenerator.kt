package com.waterplant.distribution.core.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Native Android [PdfDocument] report and invoice generation engine.
 * Generates print-ready A4 invoices and ledger reports directly on the device
 * without third-party cloud printing dependencies.
 *
 * @author Pavan Pawar
 */
class PdfGenerator @Inject constructor() {

    fun generateReport(
        context: Context,
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        fileName: String
    ): Result<File> {
        return try {
            val document = PdfDocument()
            val pageWidth = 595  // A4
            val pageHeight = 842

            val titlePaint = Paint().apply {
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.parseColor("#1565C0")
            }
            val headerPaint = Paint().apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.WHITE
            }
            val cellPaint = Paint().apply {
                textSize = 11f
                color = android.graphics.Color.parseColor("#333333")
            }
            val linePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#E0E0E0")
                strokeWidth = 0.5f
            }
            val headerBgPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#1565C0")
            }
            val altRowPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#F5F5F5")
            }

            var pageNum = 1
            var y = 80f
            val marginLeft = 40f
            val colWidth = (pageWidth - 80f) / headers.size.coerceAtLeast(1)

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            // Draw title
            canvas.drawText(title, marginLeft, 50f, titlePaint)

            // Draw headers
            canvas.drawRect(marginLeft, y - 15f, pageWidth - 40f, y + 8f, headerBgPaint)
            for (i in headers.indices) {
                canvas.drawText(headers[i], marginLeft + i * colWidth + 4f, y, headerPaint)
            }
            y += 25f

            // Draw rows
            for ((rowIdx, row) in rows.withIndex()) {
                if (y > pageHeight - 60) {
                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f
                }

                if (rowIdx % 2 == 1) {
                    canvas.drawRect(marginLeft, y - 13f, pageWidth - 40f, y + 8f, altRowPaint)
                }

                for (i in row.indices) {
                    val text = if (row[i].length > 20) row[i].take(18) + ".." else row[i]
                    canvas.drawText(text, marginLeft + i * colWidth + 4f, y, cellPaint)
                }
                canvas.drawLine(marginLeft, y + 8f, pageWidth - 40f, y + 8f, linePaint)
                y += 22f
            }

            document.finishPage(page)

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val file = File(dir, "${fileName}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

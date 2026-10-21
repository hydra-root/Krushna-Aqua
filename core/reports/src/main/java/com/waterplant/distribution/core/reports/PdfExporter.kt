package com.waterplant.distribution.core.reports

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfExporter @Inject constructor() {
    fun exportToPdf(context: Context, title: String, content: String): Result<File> {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)

            val canvas = page.canvas
            val paint = android.graphics.Paint()
            paint.textSize = 14f
            
            // Draw title and content simple text
            canvas.drawText(title, 50f, 50f, paint)
            canvas.drawText(content, 50f, 100f, paint)

            document.finishPage(page)

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "${title.replace(" ", "_")}_Report.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

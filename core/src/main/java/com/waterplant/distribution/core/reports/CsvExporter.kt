package com.waterplant.distribution.core.reports

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class CsvExporter @Inject constructor() {

    fun exportToCsv(
        context: Context,
        fileName: String,
        headers: List<String>,
        rows: List<List<String>>
    ): Result<File> {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val file = File(dir, "$fileName.csv")

            FileWriter(file).use { writer ->
                writer.append(headers.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
                writer.append("\n")
                for (row in rows) {
                    writer.append(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
                    writer.append("\n")
                }
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

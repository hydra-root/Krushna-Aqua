package com.waterplant.distribution.core.backup

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Handles SQLite database backup and restore operations.
 * 
 * In an offline-first architecture, the physical device holds the master record. 
 * This class allows the Admin to export the `.db`, `-wal` (Write-Ahead Log), 
 * and `-shm` (Shared Memory) files to external storage.
 * 
 * Backups are stamped with a timestamp and can be restored if the app is reinstalled 
 * or moved to a new device.
 */
class BackupManager @Inject constructor() {

    private val dbName = "water_jar_distribution.db"

    /**
     * Creates a full snapshot of the Room database.
     * Safely copies the `.db` file along with the `-wal` and `-shm` files to ensure
     * data integrity, since Room uses Write-Ahead Logging by default.
     */
    fun backupDatabase(context: Context): Result<File> {
        return try {
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) return Result.failure(Exception("Database not found"))

            val backupDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "backup_${timestamp}.db")

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Also backup WAL and SHM files if they exist
            val walFile = File(dbFile.path + "-wal")
            if (walFile.exists()) {
                FileInputStream(walFile).use { input ->
                    FileOutputStream(File(backupDir, "backup_${timestamp}.db-wal")).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            val shmFile = File(dbFile.path + "-shm")
            if (shmFile.exists()) {
                FileInputStream(shmFile).use { input ->
                    FileOutputStream(File(backupDir, "backup_${timestamp}.db-shm")).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun restoreDatabase(context: Context, backupFile: File): Result<Unit> {
        return try {
            val dbFile = context.getDatabasePath(dbName)

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Restore WAL file if exists
            val walBackup = File(backupFile.path.replace(".db", ".db-wal"))
            if (walBackup.exists()) {
                FileInputStream(walBackup).use { input ->
                    FileOutputStream(File(dbFile.path + "-wal")).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBackupFiles(context: Context): List<File> {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return emptyList()
        return dir.listFiles { file -> file.name.startsWith("backup_") && file.name.endsWith(".db") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }
}

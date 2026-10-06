package com.renger.system.note.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object AttachmentManager {

    /**
     * Копирует файл из Uri (полученный из системного пикера) в приватную папку приложения.
     * Возвращает абсолютный путь к сохранённому файлу или null в случае ошибки.
     */
    fun copyToAppStorage(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "attachments")
            if (!dir.exists()) dir.mkdirs()

            val originalName = getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
            val safeName = "${System.currentTimeMillis()}_${originalName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
            val outFile = File(dir, safeName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Пытается получить оригинальное имя файла из Uri.
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) { }
        return name
    }

    /**
     * Проверяет, является ли файл изображением (по расширению).
     */
    fun isImage(path: String): Boolean {
        val ext = path.substringAfterLast('.', "").lowercase()
        return ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")
    }

    /**
     * Проверяет, существует ли файл по указанному пути.
     */
    fun exists(path: String): Boolean = File(path).exists()

    /**
     * Краткое имя файла для отображения (без timestamp-префикса).
     */
    fun displayName(path: String): String {
        val fileName = File(path).name
        // Убираем префикс "1234567890_"
        return fileName.substringAfter('_', fileName)
    }

    /**
     * Удаляет файл по пути. Безопасно.
     */
    fun delete(path: String): Boolean {
        return try {
            val f = File(path)
            if (f.exists()) f.delete() else false
        } catch (_: Exception) { false }
    }
}
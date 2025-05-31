package com.example.myapplication

import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileManager {
    private val savedRates = mutableListOf<String>()
    private const val FILE_NAME_PREFIX = "currency_rates"
    private const val FILE_EXTENSION = ".txt"

    fun addRateToHistory(srcCurrency: String, dstCurrency: String, rate: Double) {
        val dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        val record = "$dateTime: 1 $srcCurrency = ${"%.4f".format(rate)} $dstCurrency"
        savedRates.add(record)
    }

    fun saveToDownloads(context: Context): Boolean {
        if (savedRates.isEmpty()) {
            Toast.makeText(context, "Нет данных для сохранения", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val fileName = "${FILE_NAME_PREFIX}_${System.currentTimeMillis()}$FILE_EXTENSION"
            val content = savedRates.joinToString("\n")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaMediaStore(context, fileName, content)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { fos ->
                    fos.write(content.toByteArray())
                }
                Toast.makeText(context, "Файл сохранен в Downloads", Toast.LENGTH_LONG).show()
            }

            clearSavedRates()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveViaMediaStore(context: Context, fileName: String, content: String) {
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(
            android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            resolver.openOutputStream(it)?.use { os ->
                os.write(content.toByteArray())
            }
            Toast.makeText(context, "Файл сохранен в Downloads", Toast.LENGTH_LONG).show()
        }
    }

    fun hasSavedRates(): Boolean = savedRates.isNotEmpty()
    fun clearSavedRates() = savedRates.clear()
}
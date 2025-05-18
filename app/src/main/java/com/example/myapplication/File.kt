import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object FileManager {
    private val savedRates = ArrayList<String>()
    private const val FILE_NAME = "saved_currency_rates.txt"

    fun addRateToHistory(context: Context, srcCurrency: String, dstCurrency: String, rate: Double) {
        val dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        val formattedRate = "%.4f".format(rate)
        val record = "$dateTime: 1 $srcCurrency = $formattedRate $dstCurrency"

        savedRates.add(record)
        showToast(context, "Курс добавлен в историю")
    }

    fun saveRatesToFile(context: Context): Boolean {
        if (savedRates.isEmpty()) {
            showToast(context, "Нет сохраненных курсов")
            return false
        }

        return try {
            val content = savedRates.joinToString("\n")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "saved_currency_rates.txt")
            Log.d("File", file.absolutePath)

            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray())
            }

            showToast(context, "Файл сохранен в ${file.absolutePath}")
            clearSavedRates()
            true
        } catch (e: Exception) {
            showToast(context, "Ошибка при сохранении файла: ${e.message}")
            false
        }
    }

    fun clearSavedRates() {
        savedRates.clear()
    }

    fun hasSavedRates(): Boolean = savedRates.isNotEmpty()

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
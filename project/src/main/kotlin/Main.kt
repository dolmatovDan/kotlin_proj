import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val rates = Rates()
    try {
        val rawData = rates.getRawRates()
        println("Данные получены успешно:")
        println(rawData)
    } catch (e: Exception) {
        println("Ошибка при получении данных: ${e.message}")
    }
}
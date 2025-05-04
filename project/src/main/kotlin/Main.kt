import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val rates = Rates()
    try {
        val currencyData = rates.getCurrencyRates()
        println("Курсы валют на ${currencyData.date}")
        currencyData.valutes.forEach {
            println("${it.charCode} (${it.name}): ${it.value} за ${it.nominal}")
        }
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
        e.printStackTrace()
    }
}
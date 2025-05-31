import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

class RatesTest {

    @Test
    fun `should load rates in reasonable time`() {
        val rates = Rates()
        val maxTimeMs = 5000

        val loadingTime = measureTimeMillis {
            val result = runBlocking { rates.loadRates() }
        }
        println("Measured time $loadingTime")

        assertTrue(loadingTime < maxTimeMs)
    }

    @Test
    fun `should return exchange rate for RUB`() {
        val rates = Rates()

        runBlocking {
            val loaded = rates.loadRates()
            val rubRate = rates.getExchangeRate("RUB", "RUB")
            assertNotNull(rubRate)
            assertEquals(rubRate, 1.0)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw for unknown currency`() {
        val rates = Rates()

        runBlocking {
            rates.loadRates()
            rates.getExchangeRate("USD", "XYZ")
        }
    }
}
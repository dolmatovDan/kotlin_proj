import android.util.Log
import android.util.Xml
import okhttp3.OkHttpClient
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.io.StringReader
import java.util.concurrent.TimeUnit

interface CbrApiService {
    @GET("scripts/XML_daily.asp")
    suspend fun getDailyRates(): String
}

class Rates {
    private val cbrApiService: CbrApiService
    private val currencyRates = mutableMapOf<String, Double>()

    init {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        cbrApiService = retrofit.create(CbrApiService::class.java)
    }

    suspend fun loadRates(): Boolean {
        return try {
            val response = cbrApiService.getDailyRates()
            if (response.isNotEmpty()) {
                parseXml(response)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getExchangeRate(fromCurrency: String, toCurrency: String): Double? {
        val fromRate = when (fromCurrency) {
            "RUB" -> 1.0
            else -> currencyRates[fromCurrency]
        }

        val toRate = when (toCurrency) {
            "RUB" -> 1.0
            else -> currencyRates[toCurrency]
        }

        if (fromRate == null || toRate == null)  {
            throw IllegalArgumentException()
        }

        return fromRate / toRate
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseXml(xmlData: String) {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlData))

        var eventType = parser.eventType
        var currentCharCode: String? = null
        var currentValue: Double? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Valute" -> {
                            currentCharCode = null
                            currentValue = null
                        }

                        "CharCode" -> {
                            parser.next()
                            currentCharCode = parser.text
                        }

                        "Value" -> {
                            parser.next()
                            currentValue = parser.text.replace(",", ".").toDouble()
                        }

                        "Nominal" -> {
                            parser.next()
                            val nominal = parser.text.replace(",", ".").toDouble()
                            if (currentValue != null) {
                                currentValue = currentValue / nominal
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "Valute" && currentCharCode != null && currentValue != null) {
                        currencyRates[currentCharCode] = currentValue
                    }
                }
            }
            eventType = parser.next()
        }

        // Добавляем рубль для конвертации
        currencyRates["RUB"] = 1.0
    }
}
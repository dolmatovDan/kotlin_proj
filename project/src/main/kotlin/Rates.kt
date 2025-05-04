import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

class Rates {
    private val xmlMapper: XmlMapper = XmlMapper().apply {
        registerKotlinModule()
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val api: CbrApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY  // Для детального логгирования
            })
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(xmlMapper))
            .build()
            .create(CbrApi::class.java)
    }

    suspend fun getCurrencyRates(): ValCurs = api.getDailyRates()

    private interface CbrApi {
        @GET("scripts/XML_daily.asp")
        suspend fun getDailyRates(): ValCurs
    }
}
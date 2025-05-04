import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit



class Rates {
    private val api: CbrApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS) // 10 сек на подключение
            .readTimeout(30, TimeUnit.SECONDS)     // 30 сек на чтение данных
            .writeTimeout(15, TimeUnit.SECONDS)    // 15 сек на отправку (если будет POST/PUT)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CbrApi::class.java)
    }

    suspend fun getRawRates(): String = api.getDailyRates()

    private interface CbrApi {
        @GET("scripts/XML_daily.asp")
        suspend fun getDailyRates(): String
    }
}
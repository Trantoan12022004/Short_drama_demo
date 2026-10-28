package com.demo.miniflicks.dramaseries.shortstory.api
import android.content.Context
import com.sulotion.security.KeyStoreManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object DramaApiIntegrator {

    private var retrofitOscar: Retrofit? = null

    fun createHeaders(context: Context): KeyStoreManager.TimeBoundApiHeaders {
        val configManagerOscar = ConfigManagerOscar(context)
        return KeyStoreManager.createHeader(
            apiKey = configManagerOscar.getApiKeyToken(),
            packageName = configManagerOscar.getPackageName(),
            deviceId = DramaBanner.getDeviceId(context),
            requestId = DramaBanner.getRequestId(),
            timestampMillis = DramaBanner.getCurrentTimeMillis(),
            apiKeyEncryption = configManagerOscar.getApiKeyEncryption()
        )
    }

    /**
     * Call this once (e.g., in Application onCreate or Activity onCreate) to get the API service
     */
    fun getService(context: Context): SeriesService {
        if (retrofitOscar != null) {
            return retrofitOscar!!.create(SeriesService::class.java)
        }

        val configManagerOscar = ConfigManagerOscar(context)
        val baseUrlOscar = configManagerOscar.getBaseUrl()

        val loggingInterceptorOscar = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val clientOscar = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptorOscar)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        retrofitOscar = Retrofit.Builder()
            .baseUrl(if (baseUrlOscar.endsWith("/")) baseUrlOscar else "$baseUrlOscar/")
            .client(clientOscar)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofitOscar!!.create(SeriesService::class.java)
    }
}
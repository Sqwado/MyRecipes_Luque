package com.example.myrecipes_luque.data.remote

import android.content.Context
import com.example.myrecipes_luque.R
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object MealDbClient {

    @Volatile
    private var apiInternal: MealDbApi? = null

    val api: MealDbApi
        get() = apiInternal
            ?: error("MealDbClient.init(context) must be called from Application.onCreate()")

    fun init(context: Context) {
        if (apiInternal != null) return

        val appContext = context.applicationContext
        val resources = appContext.resources

        val baseUrl = resources.getString(R.string.mealdb_base_url)
        val cacheSizeBytes =
            resources.getInteger(R.integer.http_cache_size_mb).toLong() * 1024L * 1024L
        val cacheMaxAgeSeconds = resources.getInteger(R.integer.http_cache_max_age_seconds)
        val cacheMaxStaleSeconds = resources.getInteger(R.integer.http_cache_max_stale_seconds)
        val connectTimeoutSeconds = resources.getInteger(R.integer.http_connect_timeout_seconds)
        val readTimeoutSeconds = resources.getInteger(R.integer.http_read_timeout_seconds)

        val cacheDir = File(appContext.cacheDir, "http_cache")
        val cache = Cache(cacheDir, cacheSizeBytes)

        val cacheInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .removeHeader("Pragma")
                .header(
                    "Cache-Control",
                    "public, max-age=$cacheMaxAgeSeconds, max-stale=$cacheMaxStaleSeconds",
                )
                .build()
        }

        val okHttp = OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor(cacheInterceptor)
            .connectTimeout(connectTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()

        apiInternal = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealDbApi::class.java)
    }
}

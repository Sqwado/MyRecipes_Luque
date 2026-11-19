package com.example.myrecipes_luque.data.remote

import android.content.Context
import com.example.myrecipes_luque.BuildConfig
import com.example.myrecipes_luque.R
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
            val request = chain.request()
            val response = chain.proceed(request)
            // random.php ne doit jamais être mis en cache (sinon Surprise ne change plus)
            if (request.url.encodedPath.endsWith("random.php")) {
                return@Interceptor response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "no-store")
                    .build()
            }
            response.newBuilder()
                .removeHeader("Pragma")
                .header(
                    "Cache-Control",
                    "public, max-age=$cacheMaxAgeSeconds, max-stale=$cacheMaxStaleSeconds",
                )
                .build()
        }

        val builder = OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor(cacheInterceptor)
            .connectTimeout(connectTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds.toLong(), TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        apiInternal = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealDbApi::class.java)
    }
}

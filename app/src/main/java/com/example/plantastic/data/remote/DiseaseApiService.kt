package com.example.plantastic.data.remote

import com.example.plantastic.BuildConfig
import com.example.plantastic.data.SettingsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Retrofit service interface for LLM API
 */
interface DiseaseDetectionApi {

    @POST("v1/chat/completions")
    suspend fun analyzePlantImage(@Body request: ChatCompletionRequest): Response<ChatCompletionResponse>
}

/**
 * Singleton object to provide configured API service.
 *
 * Configuration priority:
 * 1. Runtime settings (SettingsRepository) if useCustomApi is enabled
 * 2. BuildConfig defaults
 */
object ApiServiceProvider {

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val config = getApiConfig()
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .build()
        chain.proceed(newRequest)
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit? = null
    private var api: DiseaseDetectionApi? = null

    private fun getApiConfig(): ApiConfig {
        return try {
            if (SettingsRepository.isCustomApiConfigured()) {
                ApiConfig(
                    apiKey = SettingsRepository.apiKey,
                    baseUrl = SettingsRepository.apiBaseUrl,
                    model = SettingsRepository.apiModel
                )
            } else {
                // Fall back to BuildConfig
                ApiConfig(
                    apiKey = BuildConfig.API_KEY,
                    baseUrl = BuildConfig.API_BASE_URL,
                    model = BuildConfig.API_MODEL
                )
            }
        } catch (e: Exception) {
            // Fall back to BuildConfig if SettingsRepository is not initialized
            ApiConfig(
                apiKey = BuildConfig.API_KEY,
                baseUrl = BuildConfig.API_BASE_URL,
                model = BuildConfig.API_MODEL
            )
        }
    }

    private fun getRetrofit(): Retrofit {
        val config = getApiConfig()
        return retrofit ?: Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .also { retrofit = it }
    }

    val diseaseApi: DiseaseDetectionApi
        get() = api ?: getRetrofit().create(DiseaseDetectionApi::class.java).also { api = it }

    // For backward compatibility
    @Deprecated("Use diseaseApi instead", ReplaceWith("diseaseApi"))
    val api: DiseaseDetectionApi
        get() = diseaseApi

    val model: String
        get() = getApiConfig().model

    /**
     * Force recreate the API instance (useful when settings change)
     */
    fun recreate() {
        retrofit = null
        api = null
    }

    data class ApiConfig(
        val apiKey: String,
        val baseUrl: String,
        val model: String
    )
}

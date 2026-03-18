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
 * Singleton object to provide configured API services.
 *
 * Supports separate APIs for:
 * - Detection API: Used for plant/disease detection (vision model recommended)
 * - Chat API: Used for conversational chat (can use different/cheaper model)
 */
object ApiServiceProvider {

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Separate Retrofit and API instances for detection and chat
    private var detectionRetrofit: Retrofit? = null
    private var chatRetrofit: Retrofit? = null
    private var detectionApiInstance: DiseaseDetectionApi? = null
    private var chatApiInstance: DiseaseDetectionApi? = null

    /**
     * Get API config for detection (prioritizes detection-specific settings)
     */
    private fun getDetectionApiConfig(): ApiConfig {
        return try {
            if (SettingsRepository.useSeparateApis) {
                // Use detection-specific settings
                if (SettingsRepository.isDetectionApiConfigured()) {
                    return ApiConfig(
                        apiKey = SettingsRepository.detectionApiKey,
                        baseUrl = SettingsRepository.detectionApiBaseUrl,
                        model = SettingsRepository.detectionApiModel
                    )
                }
            }
            // Fall back to unified settings
            if (SettingsRepository.isCustomApiConfigured()) {
                ApiConfig(
                    apiKey = SettingsRepository.apiKey,
                    baseUrl = SettingsRepository.apiBaseUrl,
                    model = SettingsRepository.apiModel
                )
            } else {
                ApiConfig(
                    apiKey = BuildConfig.API_KEY,
                    baseUrl = BuildConfig.API_BASE_URL,
                    model = BuildConfig.API_MODEL
                )
            }
        } catch (e: Exception) {
            ApiConfig(
                apiKey = BuildConfig.API_KEY,
                baseUrl = BuildConfig.API_BASE_URL,
                model = BuildConfig.API_MODEL
            )
        }
    }

    /**
     * Get API config for chat (prioritizes chat-specific settings)
     */
    private fun getChatApiConfig(): ApiConfig {
        return try {
            if (SettingsRepository.useSeparateApis) {
                // Use chat-specific settings
                if (SettingsRepository.isChatApiConfigured()) {
                    return ApiConfig(
                        apiKey = SettingsRepository.chatApiKey,
                        baseUrl = SettingsRepository.chatApiBaseUrl,
                        model = SettingsRepository.chatApiModel
                    )
                }
            }
            // Fall back to unified settings
            if (SettingsRepository.isCustomApiConfigured()) {
                ApiConfig(
                    apiKey = SettingsRepository.apiKey,
                    baseUrl = SettingsRepository.apiBaseUrl,
                    model = SettingsRepository.apiModel
                )
            } else {
                ApiConfig(
                    apiKey = BuildConfig.API_KEY,
                    baseUrl = BuildConfig.API_BASE_URL,
                    model = BuildConfig.API_MODEL
                )
            }
        } catch (e: Exception) {
            ApiConfig(
                apiKey = BuildConfig.API_KEY,
                baseUrl = BuildConfig.API_BASE_URL,
                model = BuildConfig.API_MODEL
            )
        }
    }

    private fun createOkHttpClient(apiKey: String): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(newRequest)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun getDetectionRetrofit(): Retrofit {
        val config = getDetectionApiConfig()
        return detectionRetrofit ?: Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(createOkHttpClient(config.apiKey))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .also { detectionRetrofit = it }
    }

    private fun getChatRetrofit(): Retrofit {
        val config = getChatApiConfig()
        return chatRetrofit ?: Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(createOkHttpClient(config.apiKey))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .also { chatRetrofit = it }
    }

    /**
     * API for plant/disease detection (uses vision model)
     */
    val detectionApi: DiseaseDetectionApi
        get() = detectionApiInstance ?: getDetectionRetrofit().create(DiseaseDetectionApi::class.java).also { detectionApiInstance = it }

    /**
     * API for chat functionality (can use different model)
     */
    val chatApi: DiseaseDetectionApi
        get() = chatApiInstance ?: getChatRetrofit().create(DiseaseDetectionApi::class.java).also { chatApiInstance = it }

    // Legacy support - uses detectionApi
    @Deprecated("Use detectionApi instead", ReplaceWith("detectionApi"))
    val diseaseApi: DiseaseDetectionApi
        get() = detectionApi

    @Deprecated("Use detectionApi instead", ReplaceWith("detectionApi"))
    val api: DiseaseDetectionApi
        get() = detectionApi

    /**
     * Get the model name for detection
     */
    val detectionModel: String
        get() = getDetectionApiConfig().model

    /**
     * Get the model name for chat
     */
    val chatModel: String
        get() = getChatApiConfig().model

    @Deprecated("Use detectionModel instead", ReplaceWith("detectionModel"))
    val model: String
        get() = detectionModel

    /**
     * Force recreate all API instances (useful when settings change)
     */
    fun recreate() {
        detectionRetrofit = null
        chatRetrofit = null
        detectionApiInstance = null
        chatApiInstance = null
    }

    data class ApiConfig(
        val apiKey: String,
        val baseUrl: String,
        val model: String
    )
}
